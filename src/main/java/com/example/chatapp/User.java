package com.example.chatapp;

import jakarta.persistence.*;
/**
 * Verantwortungsbereich: Profilfunktionen sowie Datenbank und Persistenz.
 *
 * @author Zeynep Ünver
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    @Column(nullable = true)
    private String status = "Online";

    @Column(nullable = true)
    private String personalMessage = "";

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.status = "Online";
        this.personalMessage = "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getStatus() {
        return status != null ? status : "Online";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPersonalMessage() {
        return personalMessage != null ? personalMessage : "";
    }

    public void setPersonalMessage(String personalMessage) {
        this.personalMessage = personalMessage;
    }
}
