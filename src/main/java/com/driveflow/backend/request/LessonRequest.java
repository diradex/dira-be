package com.driveflow.backend.request;

import com.driveflow.backend.common.BaseEntity;
import com.driveflow.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LessonRequest extends BaseEntity {

    @ManyToOne
    private User student;

    @ManyToOne
    private User instructor;

    @Enumerated(EnumType.STRING)
    private LessonRequestStatus status;

    private LocalDateTime requestedStart;
    private Integer durationMinutes;
    private String pickupLocation;
    private String notes;
    
    // Track when notifications were dismissed
    private LocalDateTime notificationDismissedByStudentAt; // For ACCEPTED/REJECTED notifications
    private LocalDateTime notificationDismissedByInstructorAt; // For PENDING notifications
}
