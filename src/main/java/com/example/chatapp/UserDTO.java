package com.example.chatapp;
/**
 * Verantwortungsbereich: Backend-Entwicklung und Übertragung von Benutzerdaten.
 *
 * @author Zeynep Ünver
 */
public class UserDTO {
    private String username;
    private String profilePicture;
    private String status;
    private String personalMessage;

    public UserDTO() {}

    public UserDTO(String username, String profilePicture) {
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public UserDTO(String username, String profilePicture, String status, String personalMessage) {
        this.username = username;
        this.profilePicture = profilePicture;
        this.status = status;
        this.personalMessage = personalMessage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPersonalMessage() {
        return personalMessage;
    }

    public void setPersonalMessage(String personalMessage) {
        this.personalMessage = personalMessage;
    }
}
