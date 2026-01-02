package com.driveflow.backend.request;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRequestRepository extends JpaRepository<LessonRequest, Long> {
    List<LessonRequest> findByInstructor_Id(Long instructorId);
    List<LessonRequest> findByStudent_Id(Long studentId);
    List<LessonRequest> findByInstructor_IdAndStatus(Long instructorId, LessonRequestStatus status);
}

