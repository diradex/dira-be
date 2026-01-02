package com.driveflow.backend.request;

import com.driveflow.backend.lesson.Lesson;
import com.driveflow.backend.user.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lesson-change-requests")
@RequiredArgsConstructor
public class LessonChangeRequestController {

    private final LessonChangeRequestService service;

    @PostMapping
    public ResponseEntity<LessonChangeRequestDTO> createChangeRequest(@RequestBody CreateChangeRequestDTO dto) {
        Long studentId = getCurrentUserId();
        LessonChangeRequest request = new LessonChangeRequest();
        request.setRequestedStartTime(dto.getRequestedStartTime());
        request.setRequestedDurationMinutes(dto.getRequestedDurationMinutes());
        request.setRequestedLocation(dto.getRequestedLocation());
        request.setReason(dto.getReason());
        
        LessonChangeRequest saved = service.createChangeRequest(dto.getLessonId(), studentId, request);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @GetMapping("/instructor/pending")
    public ResponseEntity<List<LessonChangeRequestDTO>> getPendingForInstructor() {
        Long instructorId = getCurrentUserId();
        return ResponseEntity.ok(service.getPendingChangeRequestsForInstructor(instructorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/instructor")
    public ResponseEntity<List<LessonChangeRequestDTO>> getAllForInstructor() {
        Long instructorId = getCurrentUserId();
        return ResponseEntity.ok(service.getChangeRequestsForInstructor(instructorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/student")
    public ResponseEntity<List<LessonChangeRequestDTO>> getAllForStudent() {
        Long studentId = getCurrentUserId();
        return ResponseEntity.ok(service.getChangeRequestsForStudent(studentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<LessonChangeRequestDTO> acceptChangeRequest(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(service.acceptChangeRequest(id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LessonChangeRequestDTO> rejectChangeRequest(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(service.rejectChangeRequest(id)));
    }

    private Long getCurrentUserId() {
        return ((com.driveflow.backend.user.User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }

    private LessonChangeRequestDTO mapToDTO(LessonChangeRequest request) {
        User requestedBy = request.getRequestedBy();
        Lesson lesson = request.getLesson();
        
        // Fallback to lesson's student if requestedBy is null (for old records)
        String requestedByName = "Unknown";
        Long requestedById = null;
        if (requestedBy != null) {
            requestedByName = requestedBy.getFirstName() + " " + requestedBy.getLastName();
            requestedById = requestedBy.getId();
        } else if (lesson != null && lesson.getStudent() != null) {
            User student = lesson.getStudent();
            requestedByName = student.getFirstName() + " " + student.getLastName();
            requestedById = student.getId();
        }
        
        return LessonChangeRequestDTO.builder()
                .id(request.getId())
                .lessonId(lesson != null ? lesson.getId() : null)
                .requestedBy(requestedById)
                .requestedByName(requestedByName)
                .status(request.getStatus() != null ? request.getStatus().name() : "PENDING")
                .requestedStartTime(request.getRequestedStartTime())
                .requestedDurationMinutes(request.getRequestedDurationMinutes())
                .requestedLocation(request.getRequestedLocation())
                .reason(request.getReason())
                .currentStartTime(lesson != null ? lesson.getStartTime() : null)
                .currentDurationMinutes(lesson != null ? lesson.getDurationMinutes() : null)
                .currentLocation(lesson != null ? lesson.getLocation() : null)
                .build();
    }

    @Data
    public static class CreateChangeRequestDTO {
        private Long lessonId;
        private LocalDateTime requestedStartTime;
        private Integer requestedDurationMinutes;
        private String requestedLocation;
        private String reason;
    }

    @Data
    @lombok.Builder
    public static class LessonChangeRequestDTO {
        private Long id;
        private Long lessonId;
        private Long requestedBy;
        private String requestedByName;
        private String status;
        private LocalDateTime requestedStartTime;
        private Integer requestedDurationMinutes;
        private String requestedLocation;
        private String reason;
        private LocalDateTime currentStartTime;
        private Integer currentDurationMinutes;
        private String currentLocation;
    }
}

