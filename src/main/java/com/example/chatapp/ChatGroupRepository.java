package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Verantwortungsbereich: Datenbank und Persistenz.
 *
 * @author Zeynep Ünver
 */
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
}
