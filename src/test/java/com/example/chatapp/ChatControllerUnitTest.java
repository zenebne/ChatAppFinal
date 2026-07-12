/**
 * Verantwortungsbereich: Unit-Tests für den ChatController.
 *
 * @author Nilüfer Civelek
 */
package com.example.chatapp;

import com.example.chatapp.controller.ChatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerUnitTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatGroupRepository chatGroupRepository;

    private ChatController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatController(messageRepository, userRepository, chatGroupRepository);
    }

    @Test
    void registerBasariliOluncaUserKaydedilmeli() {
        User user = new User(" ali ", " 1234 ");

        when(userRepository.existsByUsername("ali")).thenReturn(false);

        ResponseEntity<String> response = controller.register(user);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("User registered successfully.");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("ali");
        assertThat(savedUser.getPassword()).isEqualTo("1234");
    }

    @Test
    void registerUsernameZatenVarsaHataDonmeli() {
        User user = new User("ali", "1234");

        when(userRepository.existsByUsername("ali")).thenReturn(true);

        ResponseEntity<String> response = controller.register(user);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Username is already taken.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginBilgileriDogruysaBasariliDonmeli() {
        User savedUser = new User("ali", "1234");
        User loginUser = new User("ali", "1234");

        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(savedUser));

        ResponseEntity<String> response = controller.login(loginUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("ali");
    }

    @Test
    void loginSifreYanlissaHataDonmeli() {
        User savedUser = new User("ali", "1234");
        User loginUser = new User("ali", "yanlis");

        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(savedUser));

        ResponseEntity<String> response = controller.login(loginUser);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("Invalid username or password.");
    }

    @Test
    void sendMessageBilgilerEksikseHataDonmeli() {
        Message message = new Message();
        message.setSender("ali");
        message.setText("Merhaba");

        ResponseEntity<String> response = controller.sendMessage(message);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Missing message details.");

        verify(messageRepository, never()).save(any(Message.class));
    }
}