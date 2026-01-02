package com.driveflow.backend.message;

import com.driveflow.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    public List<Message> getUnreadMessagesForUser(User user) {
        return messageRepository.findByReceiverAndReadFalse(user);
    }

    public List<Message> getAllMessagesForUser(User user) {
        return messageRepository.findByReceiverOrderByCreatedAtDesc(user);
    }

    public Message markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow();
        message.setRead(true);
        return messageRepository.save(message);
    }
}
