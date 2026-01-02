package com.driveflow.backend.instructor;

import com.driveflow.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InstructorProfile {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private Double pricePerLesson;
    private String serviceArea;
    private Boolean verified;

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    private java.util.List<Availability> availabilities;
}
