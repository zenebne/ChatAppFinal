package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Verantwortungsbereich: Datenbank und Persistenz.
 *
 * @author Zeynep Ünver
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderOrReceiverOrderByIdAsc(String sender, String receiver);

    @org.springframework.transaction.annotation.Transactional
    void deleteByReceiver(String receiver);
}
