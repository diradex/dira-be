package com.driveflow.backend.message;

import com.driveflow.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverAndReadFalse(User receiver);
    List<Message> findByReceiverOrderByCreatedAtDesc(User receiver);
}
