package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
/**
 * Verantwortungsbereich: Datenbank und Persistenz.
 *
 * @author Zeynep Ünver
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
