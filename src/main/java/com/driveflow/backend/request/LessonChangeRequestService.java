package com.driveflow.backend.request;

import com.driveflow.backend.lesson.Lesson;
import com.driveflow.backend.lesson.LessonRepository;
import com.driveflow.backend.user.User;
import com.driveflow.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonChangeRequestService {
    private final LessonChangeRequestRepository repository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public LessonChangeRequest createChangeRequest(Long lessonId, Long studentId, LessonChangeRequest request) {
        // Validate that requested start time is not in the past
        if (request.getRequestedStartTime() != null && request.getRequestedStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Az időpont nem lehet a múltban!");
        }
        
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        User student = userRepository.findById(studentId).orElseThrow();
        
        // Verify the student is the one requesting
        if (!lesson.getStudent().getId().equals(studentId)) {
            throw new IllegalStateException("Only the student of this lesson can request changes");
        }
        
        request.setLesson(lesson);
        request.setRequestedBy(student);
        request.setStatus(LessonChangeRequestStatus.PENDING);
        
        return repository.save(request);
    }

    public List<LessonChangeRequest> getChangeRequestsForInstructor(Long instructorId) {
        return repository.findByLesson_Instructor_Id(instructorId);
    }

    public List<LessonChangeRequest> getPendingChangeRequestsForInstructor(Long instructorId) {
        return repository.findByLesson_Instructor_IdAndStatus(instructorId, LessonChangeRequestStatus.PENDING);
    }

    public List<LessonChangeRequest> getChangeRequestsForStudent(Long studentId) {
        return repository.findByRequestedBy_Id(studentId);
    }

    @Transactional
    public LessonChangeRequest acceptChangeRequest(Long changeRequestId) {
        LessonChangeRequest changeRequest = repository.findById(changeRequestId).orElseThrow();
        
        if (changeRequest.getStatus() != LessonChangeRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending change requests can be accepted");
        }
        
        Lesson lesson = changeRequest.getLesson();
        
        // Update lesson with requested changes
        if (changeRequest.getRequestedStartTime() != null) {
            // Validate that the new start time is not in the past
            if (changeRequest.getRequestedStartTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Az időpont nem lehet a múltban!");
            }
            lesson.setStartTime(changeRequest.getRequestedStartTime());
        }
        if (changeRequest.getRequestedDurationMinutes() != null) {
            lesson.setDurationMinutes(changeRequest.getRequestedDurationMinutes());
        }
        if (changeRequest.getRequestedLocation() != null) {
            lesson.setLocation(changeRequest.getRequestedLocation());
        }
        
        lessonRepository.save(lesson);
        
        // Mark change request as accepted
        changeRequest.setStatus(LessonChangeRequestStatus.ACCEPTED);
        
        // Reject any other pending change requests for the same lesson
        List<LessonChangeRequest> otherPendingRequests = repository.findByLesson_Id(lesson.getId())
            .stream()
            .filter(req -> req.getStatus() == LessonChangeRequestStatus.PENDING && !req.getId().equals(changeRequestId))
            .toList();
        
        for (LessonChangeRequest otherRequest : otherPendingRequests) {
            otherRequest.setStatus(LessonChangeRequestStatus.REJECTED);
            repository.save(otherRequest);
        }
        
        return repository.save(changeRequest);
    }

    public LessonChangeRequest rejectChangeRequest(Long changeRequestId) {
        LessonChangeRequest changeRequest = repository.findById(changeRequestId).orElseThrow();
        
        if (changeRequest.getStatus() != LessonChangeRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending change requests can be rejected");
        }
        
        changeRequest.setStatus(LessonChangeRequestStatus.REJECTED);
        return repository.save(changeRequest);
    }
}

