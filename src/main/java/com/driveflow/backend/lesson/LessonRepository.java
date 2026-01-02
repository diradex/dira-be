package com.driveflow.backend.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByStudent_Id(Long studentId);
    List<Lesson> findByInstructor_Id(Long instructorId);
    List<Lesson> findByStudent_IdAndInstructor_IdAndStartTime(Long studentId, Long instructorId, java.time.LocalDateTime startTime);
}

