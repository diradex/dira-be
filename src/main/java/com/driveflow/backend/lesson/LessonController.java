package com.driveflow.backend.lesson;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService service;

    @GetMapping("/student/{id}")
    public ResponseEntity<List<LessonDTO>> getForStudent(@PathVariable Long id) {
        return ResponseEntity.ok(service.getLessonsForStudent(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/instructor/{id}")
    public ResponseEntity<List<LessonDTO>> getForInstructor(@PathVariable Long id) {
        return ResponseEntity.ok(service.getLessonsForInstructor(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/instructor/{id}/public")
    public ResponseEntity<List<PublicLessonDTO>> getPublicLessons(@PathVariable Long id) {
        return ResponseEntity.ok(service.getLessonsForInstructor(id).stream()
                .filter(lesson -> !"CANCELLED".equals(lesson.getStatus()) && !"REJECTED".equals(lesson.getStatus()))
                .map(lesson -> PublicLessonDTO.builder()
                        .startTime(lesson.getStartTime())
                        .durationMinutes(lesson.getDurationMinutes())
                        .type("BLOCKED".equals(lesson.getStatus()) ? "BLOCKED" : "BUSY")
                        .build())
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LessonDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(mapToDTO(service.updateStatus(id, status)));
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<LessonDTO> updateLocation(
            @PathVariable Long id,
            @RequestBody UpdateLocationDTO dto
    ) {
        return ResponseEntity.ok(mapToDTO(service.updateLocation(id, dto.getLocation())));
    }

    @PutMapping("/{id}/time")
    public ResponseEntity<LessonDTO> updateTime(
            @PathVariable Long id,
            @RequestBody UpdateTimeDTO dto
    ) {
        return ResponseEntity.ok(mapToDTO(service.updateTime(id, dto.getStartTime(), dto.getDurationMinutes())));
    }

    @PostMapping("/block")
    public ResponseEntity<LessonDTO> createBlock(@RequestBody CreateBlockDTO dto) {
        return ResponseEntity.ok(mapToDTO(service.createBlock(
                dto.getInstructorId(),
                dto.getStartTime(),
                dto.getDurationMinutes(),
                dto.getNotes()
        )));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        service.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    private LessonDTO mapToDTO(Lesson lesson) {
        Long studentId = lesson.getStudent() != null ? lesson.getStudent().getId() : null;
        String studentName = lesson.getStudent() != null 
                ? lesson.getStudent().getFirstName() + " " + lesson.getStudent().getLastName() 
                : null;

        return LessonDTO.builder()
                .id(lesson.getId())
                .studentId(studentId)
                .studentName(studentName)
                .instructorId(lesson.getInstructor().getId())
                .instructorName(lesson.getInstructor().getFirstName() + " " + lesson.getInstructor().getLastName())
                .startTime(lesson.getStartTime())
                .durationMinutes(lesson.getDurationMinutes())
                .location(lesson.getLocation())
                .status(lesson.getStatus())
                .notes(lesson.getNotes())
                .build();
    }

    @Data
    @lombok.Builder
    public static class LessonDTO {
        private Long id;
        private Long studentId;
        private String studentName;
        private Long instructorId;
        private String instructorName;
        private LocalDateTime startTime;
        private Integer durationMinutes;
        private String location;
        private String status;
        private String notes;
    }

    @Data
    @lombok.Builder
    public static class PublicLessonDTO {
        private LocalDateTime startTime;
        private Integer durationMinutes;
        private String type; // BUSY or BLOCKED
    }

    @Data
    public static class CreateBlockDTO {
        private Long instructorId;
        private LocalDateTime startTime;
        private Integer durationMinutes;
        private String notes;
    }

    @Data
    public static class UpdateLocationDTO {
        private String location;
    }

    @Data
    public static class UpdateTimeDTO {
        private LocalDateTime startTime;
        private Integer durationMinutes;
    }
}

