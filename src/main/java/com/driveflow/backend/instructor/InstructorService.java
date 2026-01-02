package com.driveflow.backend.instructor;

import com.driveflow.backend.user.User;
import com.driveflow.backend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private final InstructorProfileRepository instructorProfileRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public InstructorProfile getProfile(Long instructorId) {
        return instructorProfileRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
    }

    @Transactional
    public InstructorProfile updateProfile(Long instructorId, InstructorProfile updatedProfile) {
        InstructorProfile profile = getProfile(instructorId);
        profile.setBio(updatedProfile.getBio());
        profile.setPricePerLesson(updatedProfile.getPricePerLesson());
        profile.setServiceArea(updatedProfile.getServiceArea());
        return instructorProfileRepository.save(profile);
    }

    @Transactional
    public List<Availability> updateAvailability(Long instructorId, List<Availability> newAvailabilities) {
        InstructorProfile profile = getProfile(instructorId);
        
        // Clear existing
        availabilityRepository.deleteAll(profile.getAvailabilities());
        profile.getAvailabilities().clear();

        // Add new
        for (Availability a : newAvailabilities) {
            a.setInstructor(profile);
            profile.getAvailabilities().add(a);
        }
        
        return availabilityRepository.saveAll(profile.getAvailabilities());
    }
    public List<InstructorProfile> getAllInstructors() {
        return instructorProfileRepository.findAll();
    }
}
