package com.example.chatapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Verantwortungsbereich: Echtzeitkommunikation und WebSocket-Verarbeitung.
 *
 * @author Zeynep Ünver
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final MessageRepository messageRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(MessageRepository messageRepository, ChatGroupRepository chatGroupRepository) {
        this.messageRepository = messageRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = getUsername(session);
        if (username != null) {
            userSessions.put(username, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sender = getUsername(session);
        if (sender == null) return;

        // Parse incoming message
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String receiver = payload.get("receiver");
        String text = payload.get("text");

        if (receiver == null || text == null || text.trim().isEmpty()) return;

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Message chatMessage = new Message(sender, receiver, text, time);
        chatMessage = messageRepository.save(chatMessage);

        String jsonResponse = objectMapper.writeValueAsString(chatMessage);
        TextMessage responseMessage = new TextMessage(jsonResponse);

        if (receiver.startsWith("group_")) {
            // Broadcast only to group members
            try {
                Long groupId = Long.parseLong(receiver.substring(6));
                java.util.Optional<ChatGroup> groupOpt = chatGroupRepository.findById(groupId);
                if (groupOpt.isPresent()) {
                    ChatGroup group = groupOpt.get();
                    for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
                        String username = entry.getKey();
                        WebSocketSession userSession = entry.getValue();
                        boolean isMember = username.equalsIgnoreCase(group.getCreator()) || 
                                           (group.getMembers() != null && java.util.Arrays.asList(group.getMembers().split(",")).stream().anyMatch(username::equalsIgnoreCase));
                        if (isMember && userSession.isOpen()) {
                            userSession.sendMessage(responseMessage);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Send to receiver if online
            WebSocketSession receiverSession = userSessions.get(receiver);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(responseMessage);
            }

            // Send back to sender to confirm/render
            if (session.isOpen()) {
                session.sendMessage(responseMessage);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = getUsername(session);
        if (username != null) {
            userSessions.remove(username);
        }
    }

    private String getUsername(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        
        String query = uri.getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1 && "user".equals(entry[0])) {
                return entry[1];
            }
        }
        return null;
    }
}
