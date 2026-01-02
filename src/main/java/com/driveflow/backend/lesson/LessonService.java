package com.driveflow.backend.lesson;

import com.driveflow.backend.user.User;
import com.driveflow.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public List<Lesson> getLessonsForStudent(Long studentId) {
        return lessonRepository.findByStudent_Id(studentId);
    }

    public List<Lesson> getLessonsForInstructor(Long instructorId) {
        return lessonRepository.findByInstructor_Id(instructorId);
    }

    public Lesson createBlock(Long instructorId, LocalDateTime startTime, Integer durationMinutes, String notes) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (startTime != null && startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Az időpont nem lehet a múltban!");
        }

        Lesson block = Lesson.builder()
                .instructor(instructor)
                .startTime(startTime)
                .durationMinutes(durationMinutes)
                .status("BLOCKED")
                .notes(notes)
                .location("N/A") // Or leave null
                .completed(false)
                .build();

        return lessonRepository.save(block);
    }

    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    public Lesson updateStatus(Long lessonId, String status) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        lesson.setStatus(status);
        if ("COMPLETED".equals(status)) {
            lesson.setCompleted(true);
        }
        return lessonRepository.save(lesson);
    }

    public Lesson updateLocation(Long lessonId, String location) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        lesson.setLocation(location);
        return lessonRepository.save(lesson);
    }

    public Lesson updateTime(Long lessonId, LocalDateTime startTime, Integer durationMinutes) {
        // Validate that start time is not in the past
        if (startTime != null && startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Az időpont nem lehet a múltban!");
        }
        
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        lesson.setStartTime(startTime);
        if (durationMinutes != null) {
            lesson.setDurationMinutes(durationMinutes);
        }
        return lessonRepository.save(lesson);
    }
}

