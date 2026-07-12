/**
 * Verantwortungsbereich: Unit-Tests für die Modellklassen.
 *
 * @author Nilüfer Civelek
 */
package com.example.chatapp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelUnitTest {

    @Test
    void user_shouldUseDefaultStatusAndPersonalMessage() {
        User user = new User("alice", "secret");

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getStatus()).isEqualTo("Online");
        assertThat(user.getPersonalMessage()).isEqualTo("");
    }

    @Test
    void message_shouldStoreAllFields() {
        Message message = new Message("alice", "bob", "Hello", "12:30");
        message.setId(1L);

        assertThat(message.getId()).isEqualTo(1L);
        assertThat(message.getSender()).isEqualTo("alice");
        assertThat(message.getReceiver()).isEqualTo("bob");
        assertThat(message.getText()).isEqualTo("Hello");
        assertThat(message.getTime()).isEqualTo("12:30");
    }

    @Test
    void chatGroup_shouldStoreAllFields() {
        ChatGroup group = new ChatGroup("Project", "alice", "bob,carol");
        group.setId(10L);

        assertThat(group.getId()).isEqualTo(10L);
        assertThat(group.getName()).isEqualTo("Project");
        assertThat(group.getCreator()).isEqualTo("alice");
        assertThat(group.getMembers()).isEqualTo("bob,carol");
    }
}