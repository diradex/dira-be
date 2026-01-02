package com.driveflow.backend.request;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonChangeRequestRepository extends JpaRepository<LessonChangeRequest, Long> {
    List<LessonChangeRequest> findByLesson_Id(Long lessonId);
    List<LessonChangeRequest> findByLesson_Instructor_Id(Long instructorId);
    List<LessonChangeRequest> findByLesson_Instructor_IdAndStatus(Long instructorId, LessonChangeRequestStatus status);
    List<LessonChangeRequest> findByRequestedBy_Id(Long studentId);
    List<LessonChangeRequest> findByRequestedBy_IdAndStatus(Long studentId, LessonChangeRequestStatus status);
}

