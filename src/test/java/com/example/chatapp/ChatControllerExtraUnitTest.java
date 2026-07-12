/**
 * Verantwortungsbereich: Zusätzliche Unit-Tests für den ChatController.
 *
 * @author Nilüfer Civelek
 */
package com.example.chatapp;

import com.example.chatapp.controller.ChatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerExtraUnitTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatGroupRepository chatGroupRepository;

    private ChatController controller;
    private Path uploadDir;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ChatController(messageRepository, userRepository, chatGroupRepository);

        uploadDir = Files.createTempDirectory("chatapp-test-uploads");
        ReflectionTestUtils.setField(controller, "uploadDir", uploadDir.toString());
    }

    @Test
    void updateStatusBasariliOluncaUserKaydedilmeli() {
        User user = new User("ali", "1234");

        UserDTO request = new UserDTO();
        request.setUsername("ali");
        request.setStatus("Busy");

        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = controller.updateStatus(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Status updated successfully.");
        assertThat(user.getStatus()).isEqualTo("Busy");

        verify(userRepository).save(user);
    }

    @Test
    void updateStatusUserYoksa404Donmeli() {
        UserDTO request = new UserDTO();
        request.setUsername("ali");
        request.setStatus("Away");

        when(userRepository.findByUsername("ali")).thenReturn(Optional.empty());

        ResponseEntity<String> response = controller.updateStatus(request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isEqualTo("User not found.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePersonalMessageBasariliOluncaUserKaydedilmeli() {
        User user = new User("ali", "1234");

        UserDTO request = new UserDTO();
        request.setUsername("ali");
        request.setPersonalMessage("Bugün meşgulüm");

        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = controller.updatePersonalMessage(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Personal message updated successfully.");
        assertThat(user.getPersonalMessage()).isEqualTo("Bugün meşgulüm");

        verify(userRepository).save(user);
    }

    @Test
    void uploadProfilePictureBasariliOluncaDosyaKaydedilmeli() {
        User user = new User("ali", "1234");

        String imageBase64 = Base64.getEncoder()
                .encodeToString("fake-image-data".getBytes(StandardCharsets.UTF_8));

        UserDTO request = new UserDTO();
        request.setUsername("ali");
        request.setProfilePicture("data:image/png;base64," + imageBase64);

        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = controller.uploadProfilePicture(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        String responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).startsWith("/media/profile_");
        assertThat(responseBody).endsWith(".png");
        assertThat(user.getProfilePicture()).isEqualTo(responseBody);

        String filename = responseBody.replace("/media/", "");
        assertThat(Files.exists(uploadDir.resolve(filename))).isTrue();

        verify(userRepository).save(user);
    }

    @Test
    void uploadProfilePictureBosResimGelirse400Donmeli() {
        UserDTO request = new UserDTO();
        request.setUsername("ali");
        request.setProfilePicture("");

        ResponseEntity<String> response = controller.uploadProfilePicture(request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Image data cannot be empty.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteGroupBasariliOluncaGroupVeMesajlarSilinmeli() {
        ChatGroup group = new ChatGroup("Grup1", "ali", "veli,ayse");
        group.setId(5L);

        when(chatGroupRepository.findById(5L)).thenReturn(Optional.of(group));

        ResponseEntity<String> response = controller.deleteGroup(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Deleted");

        verify(chatGroupRepository).deleteById(5L);
        verify(messageRepository).deleteByReceiver("group_5");
    }

    @Test
    void deleteGroupBulunamazsa404Donmeli() {
        when(chatGroupRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = controller.deleteGroup(99L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isEqualTo("Group not found");

        verify(chatGroupRepository, never()).deleteById(anyLong());
        verify(messageRepository, never()).deleteByReceiver(anyString());
    }
}