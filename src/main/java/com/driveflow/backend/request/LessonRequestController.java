package com.driveflow.backend.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class LessonRequestController {

    private final LessonRequestService service;

    @PostMapping
    public ResponseEntity<LessonRequestDTO> createRequest(@RequestBody CreateRequestDTO dto) {
        LessonRequest req = new LessonRequest();
        req.setRequestedStart(dto.getRequestedStart());
        req.setDurationMinutes(dto.getDurationMinutes());
        req.setPickupLocation(dto.getPickupLocation());
        req.setNotes(dto.getNotes());

        LessonRequest saved = service.createRequest(dto.getStudentId(), dto.getInstructorId(), req);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @GetMapping("/instructor/{id}")
    public ResponseEntity<List<LessonRequestDTO>> getForInstructor(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRequestsForInstructor(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<List<LessonRequestDTO>> getForStudent(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRequestsForStudent(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LessonRequestDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam LessonRequestStatus status
    ) {
        return ResponseEntity.ok(mapToDTO(service.updateStatus(id, status)));
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<LessonRequestDTO> updateLocation(
            @PathVariable Long id,
            @RequestBody UpdateLocationDTO dto
    ) {
        return ResponseEntity.ok(mapToDTO(service.updateLocation(id, dto.getLocation())));
    }

    private LessonRequestDTO mapToDTO(LessonRequest req) {
        return LessonRequestDTO.builder()
                .id(req.getId())
                .studentId(req.getStudent().getId())
                .studentName(req.getStudent().getFirstName() + " " + req.getStudent().getLastName())
                .instructorId(req.getInstructor().getId())
                .instructorName(req.getInstructor().getFirstName() + " " + req.getInstructor().getLastName())
                .status(req.getStatus())
                .requestedStart(req.getRequestedStart())
                .durationMinutes(req.getDurationMinutes())
                .pickupLocation(req.getPickupLocation())
                .notes(req.getNotes())
                .build();
    }

    @Data
    public static class CreateRequestDTO {
        private Long studentId;
        private Long instructorId;
        private LocalDateTime requestedStart;
        private Integer durationMinutes;
        private String pickupLocation;
        private String notes;
    }

    @Data
    public static class UpdateLocationDTO {
        private String location;
    }

    @Data
    @lombok.Builder
    public static class LessonRequestDTO {
        private Long id;
        private Long studentId;
        private String studentName;
        private Long instructorId;
        private String instructorName;
        private LessonRequestStatus status;
        private LocalDateTime requestedStart;
        private Integer durationMinutes;
        private String pickupLocation;
        private String notes;
    }
}

