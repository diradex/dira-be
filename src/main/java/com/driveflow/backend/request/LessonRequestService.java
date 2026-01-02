package com.driveflow.backend.request;

import com.driveflow.backend.lesson.Lesson;
import com.driveflow.backend.lesson.LessonRepository;
import com.driveflow.backend.user.User;
import com.driveflow.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonRequestService {
    private final LessonRequestRepository repository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public LessonRequest createRequest(Long studentId, Long instructorId, LessonRequest req) {
        // Validate that requested start time is not in the past
        if (req.getRequestedStart() != null && req.getRequestedStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Az időpont nem lehet a múltban!");
        }
        
        User student = userRepository.findById(studentId).orElseThrow();
        User instructor = userRepository.findById(instructorId).orElseThrow();

        req.setStudent(student);
        req.setInstructor(instructor);
        req.setStatus(LessonRequestStatus.PENDING);
        
        return repository.save(req);
    }

    public List<LessonRequest> getRequestsForInstructor(Long instructorId) {
        return repository.findByInstructor_Id(instructorId);
    }

    public List<LessonRequest> getRequestsForStudent(Long studentId) {
        return repository.findByStudent_Id(studentId);
    }

    public LessonRequest updateStatus(Long requestId, LessonRequestStatus status) {
        LessonRequest req = repository.findById(requestId).orElseThrow();
        req.setStatus(status);
        
        LessonRequest saved = repository.save(req);

        if (status == LessonRequestStatus.ACCEPTED) {
            createLessonFromRequest(saved);
        }

        return saved;
    }

    public LessonRequest updateLocation(Long requestId, String location) {
        LessonRequest req = repository.findById(requestId).orElseThrow();
        
        // Only allow location update if request is accepted
        if (req.getStatus() != LessonRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Location can only be set for accepted requests");
        }
        
        req.setPickupLocation(location);
        
        // Also update the lesson location if it exists (find by matching student, instructor, and startTime)
        List<Lesson> lessons = lessonRepository.findByStudent_IdAndInstructor_IdAndStartTime(
                req.getStudent().getId(), 
                req.getInstructor().getId(), 
                req.getRequestedStart());
        if (!lessons.isEmpty()) {
            Lesson lesson = lessons.get(0);
            lesson.setLocation(location);
            lessonRepository.save(lesson);
        }
        
        return repository.save(req);
    }

    private void createLessonFromRequest(LessonRequest req) {
        Lesson lesson = Lesson.builder()
                .student(req.getStudent())
                .instructor(req.getInstructor())
                .startTime(req.getRequestedStart())
                .durationMinutes(req.getDurationMinutes())
                .location(req.getPickupLocation())
                .status("SCHEDULED")
                .notes(req.getNotes())
                .build();
        lessonRepository.save(lesson);
    }
}

