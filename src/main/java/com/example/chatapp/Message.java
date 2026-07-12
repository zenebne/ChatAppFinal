package com.example.chatapp;

import jakarta.persistence.*;
/**
 * Verantwortungsbereich: Backend-Entwicklung sowie Datenbank und Persistenz.
 *
 * @author Zeynep Ünver
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private String time;

    public Message() {}

    public Message(String sender, String receiver, String text, String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.time = time;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getText() { return text; }
    public String getTime() { return time; }

    public void setSender(String sender) { this.sender = sender; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public void setText(String text) { this.text = text; }
    public void setTime(String time) { this.time = time; }
}