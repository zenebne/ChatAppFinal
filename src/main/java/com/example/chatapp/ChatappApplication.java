package com.example.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Verantwortungsbereich: Gemeinsame Integration und Startkonfiguration der Anwendung.
 *
 * @author Zeynep Ünver
 * @author Nilüfer Civelek
 */
@SpringBootApplication
public class ChatappApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatappApplication.class, args);
	}

}