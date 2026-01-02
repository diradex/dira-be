package com.driveflow.backend.instructor;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService service;

    @GetMapping
    public ResponseEntity<List<InstructorProfileDTO>> getAllInstructors(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return ResponseEntity.ok(service.getAllInstructors().stream()
                .filter(p -> {
                    if (location == null || location.isBlank()) return true;
                    return p.getServiceArea() != null && p.getServiceArea().toLowerCase().contains(location.trim().toLowerCase());
                })
                .filter(p -> {
                    if (minPrice == null || minPrice <= 0) return true;
                    if (p.getPricePerLesson() == null) return false;
                    return p.getPricePerLesson() >= minPrice;
                })
                .filter(p -> {
                    if (maxPrice == null || maxPrice >= 1000) return true; // High enough default
                    if (p.getPricePerLesson() == null) return true; // Show if no price set and max filter is loose
                    return p.getPricePerLesson() <= maxPrice;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorProfileDTO> getProfile(@PathVariable Long id) {
        InstructorProfile profile = service.getProfile(id);
        return ResponseEntity.ok(mapToDTO(profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstructorProfileDTO> updateProfile(
            @PathVariable Long id,
            @RequestBody InstructorProfileDTO dto
    ) {
        InstructorProfile updated = InstructorProfile.builder()
                .bio(dto.getBio())
                .pricePerLesson(dto.getPricePerLesson())
                .serviceArea(dto.getServiceArea())
                .build();
        
        return ResponseEntity.ok(mapToDTO(service.updateProfile(id, updated)));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<List<AvailabilityDTO>> updateAvailability(
            @PathVariable Long id,
            @RequestBody List<AvailabilityDTO> dtos
    ) {
        List<Availability> domains = dtos.stream().map(dto -> Availability.builder()
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build()).collect(Collectors.toList());

        List<Availability> saved = service.updateAvailability(id, domains);
        
        return ResponseEntity.ok(saved.stream().map(this::mapToAvailabilityDTO).collect(Collectors.toList()));
    }

    private InstructorProfileDTO mapToDTO(InstructorProfile p) {
        return InstructorProfileDTO.builder()
                .userId(p.getUserId())
                .firstName(p.getUser().getFirstName())
                .lastName(p.getUser().getLastName())
                .bio(p.getBio())
                .pricePerLesson(p.getPricePerLesson())
                .serviceArea(p.getServiceArea())
                .availabilities(p.getAvailabilities() != null ? 
                        p.getAvailabilities().stream().map(this::mapToAvailabilityDTO).collect(Collectors.toList()) : List.of())
                .build();
    }

    private AvailabilityDTO mapToAvailabilityDTO(Availability a) {
        return AvailabilityDTO.builder()
                .dayOfWeek(a.getDayOfWeek())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .build();
    }

    @Data
    @lombok.Builder
    public static class InstructorProfileDTO {
        private Long userId;
        private String firstName;
        private String lastName;
        private String bio;
        private Double pricePerLesson;
        private String serviceArea;
        private List<AvailabilityDTO> availabilities;
    }

    @Data
    @lombok.Builder
    public static class AvailabilityDTO {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}

