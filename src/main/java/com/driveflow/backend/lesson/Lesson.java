package com.driveflow.backend.lesson;

import com.driveflow.backend.common.BaseEntity;
import com.driveflow.backend.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
public class Lesson extends BaseEntity {

    @ManyToOne
    private User student;

    @ManyToOne
    private User instructor;

    private LocalDateTime startTime;
    private Integer durationMinutes;
    private String location;
    private String status;
    private String notes;
    private Boolean completed;
}
