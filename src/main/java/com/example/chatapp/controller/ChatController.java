package com.example.chatapp.controller;

import com.example.chatapp.Message;
import com.example.chatapp.MessageRepository;
import com.example.chatapp.User;
import com.example.chatapp.UserDTO;
import com.example.chatapp.UserRepository;
import com.example.chatapp.ChatGroup;
import com.example.chatapp.ChatGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
/**
 * Verantwortungsbereich: Backend entwicklung und Profilfunktionen.
 * @author Zeynep Ünver
 */

@RestController
@CrossOrigin // Allow cross-origin requests for testing across devices
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ChatController(MessageRepository messageRepository, UserRepository userRepository, ChatGroupRepository chatGroupRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        String username = user.getUsername().trim();
        String password = user.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password cannot be empty.");
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username is already taken.");
        }

        userRepository.save(new User(username, password));
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest) {
        String username = loginRequest.getUsername().trim();
        String password = loginRequest.getPassword().trim();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return ResponseEntity.ok(username);
        }

        return ResponseEntity.status(401).body("Invalid username or password.");
    }

    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getUsername(), user.getProfilePicture(), user.getStatus(), user.getPersonalMessage()))
                .toList();
    }

    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestBody UserDTO request) {
        String username = request.getUsername();
        String status = request.getStatus();
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty.");
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status != null ? status : "Online");
            userRepository.save(user);
            return ResponseEntity.ok("Status updated successfully.");
        }
        return ResponseEntity.status(404).body("User not found.");
    }

    @PostMapping("/updatePersonalMessage")
    public ResponseEntity<String> updatePersonalMessage(@RequestBody UserDTO request) {
        String username = request.getUsername();
        String personalMsg = request.getPersonalMessage();
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty.");
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPersonalMessage(personalMsg != null ? personalMsg : "");
            userRepository.save(user);
            return ResponseEntity.ok("Personal message updated successfully.");
        }
        return ResponseEntity.status(404).body("User not found.");
    }

    @PostMapping("/uploadProfilePicture")
    public ResponseEntity<String> uploadProfilePicture(@RequestBody UserDTO request) {
        String username = request.getUsername();
        String profilePicture = request.getProfilePicture();

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty.");
        }
        if (profilePicture == null || profilePicture.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Image data cannot be empty.");
        }

        try {
            // Ensure directory exists
            java.nio.file.Path directory = java.nio.file.Paths.get(uploadDir);
            java.nio.file.Files.createDirectories(directory);

            // Clean base64 header if present
            String base64Data = profilePicture;
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }

            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            String safeFilename = "profile_" + java.util.UUID.nameUUIDFromBytes(username.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString() + ".png";
            java.nio.file.Path filePath = directory.resolve(safeFilename);
            java.nio.file.Files.write(filePath, imageBytes);

            // Update user profile url in database
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Store URL instead of raw base64 string
                String imageUrl = "/media/" + safeFilename;
                user.setProfilePicture(imageUrl);
                userRepository.save(user);
                return ResponseEntity.ok(imageUrl);
            }
            return ResponseEntity.status(404).body("User not found.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error saving profile picture: " + e.getMessage());
        }
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestBody Message message) {
        if (message.getSender() == null || message.getReceiver() == null || message.getText() == null) {
            return ResponseEntity.badRequest().body("Missing message details.");
        }

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Message savedMessage = new Message(
                message.getSender(),
                message.getReceiver(),
                message.getText(),
                time
        );

        messageRepository.save(savedMessage);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/messages")
    public List<Message> getMessages(@RequestParam String user) {
        return messageRepository.findBySenderOrReceiverOrderByIdAsc(user, user);
    }

    @PostMapping("/groups")
    public ResponseEntity<ChatGroup> createGroup(@RequestBody ChatGroup chatGroup) {
        if (chatGroup.getName() == null || chatGroup.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ChatGroup saved = chatGroupRepository.save(chatGroup);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/groups")
    public List<ChatGroup> getGroups(@RequestParam(required = false) String user) {
        List<ChatGroup> allGroups = chatGroupRepository.findAll();
        if (user == null || user.trim().isEmpty()) {
            return allGroups;
        }
        final String searchUser = user.trim();
        return allGroups.stream()
                .filter(g -> g.getCreator().equalsIgnoreCase(searchUser) || 
                            (g.getMembers() != null && java.util.Arrays.asList(g.getMembers().split(",")).stream().anyMatch(searchUser::equalsIgnoreCase)))
                .toList();
    }

    @DeleteMapping("/groups/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> deleteGroup(@PathVariable Long id) {
        Optional<ChatGroup> groupOpt = chatGroupRepository.findById(id);
        if (groupOpt.isPresent()) {
            chatGroupRepository.deleteById(id);
            // Delete all messages sent to this group (receiver matches group_<id>)
            messageRepository.deleteByReceiver("group_" + id);
            return ResponseEntity.ok("Deleted");
        }
        return ResponseEntity.status(404).body("Group not found");
    }
}
