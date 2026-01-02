package com.driveflow.backend.request;

import com.driveflow.backend.common.BaseEntity;
import com.driveflow.backend.lesson.Lesson;
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
public class LessonChangeRequest extends BaseEntity {

    @ManyToOne
    private Lesson lesson;

    @ManyToOne
    private User requestedBy; // Student who requested the change

    @Enumerated(EnumType.STRING)
    private LessonChangeRequestStatus status;

    private LocalDateTime requestedStartTime; // New requested time (null if not changing)
    private Integer requestedDurationMinutes; // New requested duration (null if not changing)
    private String requestedLocation; // New requested location (null if not changing)
    
    private String reason; // Optional reason for the change request
}

