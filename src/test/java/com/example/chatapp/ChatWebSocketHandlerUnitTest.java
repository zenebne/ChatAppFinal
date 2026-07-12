/**
 * Verantwortungsbereich: Unit-Tests für die WebSocket-Kommunikation.
 *
 * @author Nilüfer Civelek
 */
package com.example.chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerUnitTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatGroupRepository chatGroupRepository;

    private ChatWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChatWebSocketHandler(messageRepository, chatGroupRepository);
    }

    @Test
    void directMessageGelinceMesajKaydedilmeliVeAliciyaGonderilmeli() throws Exception {
        WebSocketSession aliSession = createSession("ali");
        WebSocketSession veliSession = createSession("veli");

        handler.afterConnectionEstablished(aliSession);
        handler.afterConnectionEstablished(veliSession);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TextMessage message = new TextMessage("""
                {
                  "receiver": "veli",
                  "text": "Merhaba"
                }
                """);

        handler.handleTextMessage(aliSession, message);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();

        assertThat(savedMessage.getSender()).isEqualTo("ali");
        assertThat(savedMessage.getReceiver()).isEqualTo("veli");
        assertThat(savedMessage.getText()).isEqualTo("Merhaba");
        assertThat(savedMessage.getTime()).isNotBlank();

        verify(veliSession).sendMessage(any(TextMessage.class));
        verify(aliSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void bosMesajGelirseKayitVeGonderimYapilmamali() throws Exception {
        WebSocketSession aliSession = createSession("ali");

        handler.afterConnectionEstablished(aliSession);

        TextMessage message = new TextMessage("""
                {
                  "receiver": "veli",
                  "text": "   "
                }
                """);

        handler.handleTextMessage(aliSession, message);

        verify(messageRepository, never()).save(any(Message.class));
        verify(aliSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void groupMessageSadeceGrupUyelerineGonderilmeli() throws Exception {
        WebSocketSession aliSession = createSession("ali");
        WebSocketSession veliSession = createSession("veli");
        WebSocketSession ayseSession = createSession("ayse");
        WebSocketSession mehmetSession = createSession("mehmet");

        handler.afterConnectionEstablished(aliSession);
        handler.afterConnectionEstablished(veliSession);
        handler.afterConnectionEstablished(ayseSession);
        handler.afterConnectionEstablished(mehmetSession);

        ChatGroup group = new ChatGroup("Grup1", "ali", "veli,ayse");
        group.setId(1L);

        when(chatGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TextMessage message = new TextMessage("""
                {
                  "receiver": "group_1",
                  "text": "Herkese merhaba"
                }
                """);

        handler.handleTextMessage(aliSession, message);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();

        assertThat(savedMessage.getSender()).isEqualTo("ali");
        assertThat(savedMessage.getReceiver()).isEqualTo("group_1");
        assertThat(savedMessage.getText()).isEqualTo("Herkese merhaba");

        verify(aliSession).sendMessage(any(TextMessage.class));
        verify(veliSession).sendMessage(any(TextMessage.class));
        verify(ayseSession).sendMessage(any(TextMessage.class));
        verify(mehmetSession, never()).sendMessage(any(TextMessage.class));
    }

    private WebSocketSession createSession(String username) {
        WebSocketSession session = mock(WebSocketSession.class);

        URI uri = URI.create("ws://localhost/chat?user=" + username);
        doReturn(uri).when(session).getUri();

        lenient().when(session.isOpen()).thenReturn(true);

        return session;
    }
}