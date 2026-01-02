package com.driveflow.backend.notification;

import com.driveflow.backend.message.MessageService;
import com.driveflow.backend.request.LessonRequest;
import com.driveflow.backend.request.LessonRequestRepository;
import com.driveflow.backend.request.LessonRequestStatus;
import com.driveflow.backend.user.User;
import com.driveflow.backend.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final MessageService messageService;
    private final LessonRequestRepository requestRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<NotificationSummaryDTO> getNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(403).build();
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<NotificationDTO> notifications = collectNotifications(user);
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

        return ResponseEntity.ok(NotificationSummaryDTO.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestParam String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(403).build();
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        if ("message".equals(type) || "MESSAGE".equals(type)) {
            messageService.markAsRead(id);
        } else if ("lesson_request".equals(type) || "LESSON_REQUEST".equals(type)) {
            // Mark as dismissed by instructor
            requestRepository.findById(id).ifPresent(request -> {
                if (request.getInstructor().getId().equals(user.getId())) {
                    request.setNotificationDismissedByInstructorAt(LocalDateTime.now());
                    requestRepository.save(request);
                }
            });
        } else if ("lesson_accepted".equals(type) || "LESSON_ACCEPTED".equals(type) ||
                   "lesson_rejected".equals(type) || "LESSON_REJECTED".equals(type)) {
            // Mark as dismissed by student
            requestRepository.findById(id).ifPresent(request -> {
                if (request.getStudent().getId().equals(user.getId())) {
                    request.setNotificationDismissedByStudentAt(LocalDateTime.now());
                    requestRepository.save(request);
                }
            });
        }
        return ResponseEntity.ok().build();
    }

    private List<NotificationDTO> collectNotifications(User user) {
        List<NotificationDTO> notifications = new java.util.ArrayList<>();

        // Collect unread messages
        messageService.getUnreadMessagesForUser(user).forEach(message -> {
            notifications.add(NotificationDTO.builder()
                    .id(message.getId())
                    .type("MESSAGE")
                    .title("Új üzenet")
                    .message(message.getContent())
                    .fromUser(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                    .createdAt(message.getCreatedAt())
                    .read(false)
                    .build());
        });

        // Collect pending lesson requests (only if not dismissed by instructor)
        if ("INSTRUCTOR".equals(user.getRole().name())) {
            List<LessonRequest> pendingRequests = requestRepository.findByInstructor_IdAndStatus(
                    user.getId(), LessonRequestStatus.PENDING);
            pendingRequests.stream()
                    .filter(request -> request.getNotificationDismissedByInstructorAt() == null)
                    .forEach(request -> {
                        notifications.add(NotificationDTO.builder()
                                .id(request.getId())
                                .type("LESSON_REQUEST")
                                .title("Új óra kérés")
                                .message(request.getStudent().getFirstName() + " " + request.getStudent().getLastName() +
                                        " óra kérést küldött")
                                .fromUser(request.getStudent().getFirstName() + " " + request.getStudent().getLastName())
                                .createdAt(request.getCreatedAt())
                                .read(false)
                                .build());
                    });
        } else if ("STUDENT".equals(user.getRole().name())) {
            List<LessonRequest> studentRequests = requestRepository.findByStudent_Id(user.getId());
            studentRequests.stream()
                    .filter(request -> request.getNotificationDismissedByStudentAt() == null)
                    .forEach(request -> {
                        if (request.getStatus() == LessonRequestStatus.ACCEPTED) {
                            notifications.add(NotificationDTO.builder()
                                    .id(request.getId())
                                    .type("LESSON_ACCEPTED")
                                    .title("Óra kérés elfogadva")
                                    .message(request.getInstructor().getFirstName() + " " + request.getInstructor().getLastName() +
                                            " elfogadta az óra kérésedet")
                                    .fromUser(request.getInstructor().getFirstName() + " " + request.getInstructor().getLastName())
                                    .createdAt(request.getUpdatedAt() != null ? request.getUpdatedAt() : request.getCreatedAt())
                                    .read(false)
                                    .build());
                        } else if (request.getStatus() == LessonRequestStatus.REJECTED) {
                            notifications.add(NotificationDTO.builder()
                                    .id(request.getId())
                                    .type("LESSON_REJECTED")
                                    .title("Óra kérés elutasítva")
                                    .message(request.getInstructor().getFirstName() + " " + request.getInstructor().getLastName() +
                                            " elutasította az óra kérésedet")
                                    .fromUser(request.getInstructor().getFirstName() + " " + request.getInstructor().getLastName())
                                    .createdAt(request.getUpdatedAt() != null ? request.getUpdatedAt() : request.getCreatedAt())
                                    .read(false)
                                    .build());
                        }
                    });
        }

        // Sort by creation date, newest first
        notifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return notifications;
    }

    @Data
    @Builder
    public static class NotificationDTO {
        private Long id;
        private String type; // MESSAGE, LESSON_REQUEST, LESSON_ACCEPTED, LESSON_REJECTED
        private String title;
        private String message;
        private String fromUser;
        private LocalDateTime createdAt;
        private boolean read;
    }

    @Data
    @Builder
    public static class NotificationSummaryDTO {
        private List<NotificationDTO> notifications;
        private long unreadCount;
    }
}

