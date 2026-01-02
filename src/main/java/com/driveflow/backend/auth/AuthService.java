package com.driveflow.backend.auth;

import com.driveflow.backend.email.EmailService;
import com.driveflow.backend.instructor.InstructorProfile;
import com.driveflow.backend.instructor.InstructorProfileRepository;
import com.driveflow.backend.student.StudentProfile;
import com.driveflow.backend.student.StudentProfileRepository;
import com.driveflow.backend.user.Role;
import com.driveflow.backend.user.User;
import com.driveflow.backend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final InstructorProfileRepository instructorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String token = UUID.randomUUID().toString();
        logger.info("Generated verification token: {}", token);

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .enabled(false)
                .verificationToken(token)
                .build();
        User savedUser = userRepository.saveAndFlush(user);
        logger.info("User registered with email: {} and token: {}", savedUser.getEmail(), savedUser.getVerificationToken());

        if (user.getRole() == Role.STUDENT) {
            StudentProfile studentProfile = StudentProfile.builder()
                    .user(savedUser)
                    .build();
            studentProfileRepository.save(studentProfile);
        } else if (user.getRole() == Role.INSTRUCTOR) {
            InstructorProfile instructorProfile = InstructorProfile.builder()
                    .user(savedUser)
                    .verified(false) // Instructors need verification
                    .build();
            instructorProfileRepository.save(instructorProfile);
        }

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFirstName(),
                token
        );
        
        return AuthenticationResponse.builder()
                .token(null) // Do not return token until verified
                .build();
    }

    @Transactional
    public void verifyToken(String token) {
        logger.info("Attempting to verify token: {}", token);
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> {
                    logger.warn("Verification failed: Token invalid or expired for token: {}", token);
                    return new IllegalStateException("Token invalid or expired");
                });
        
        user.setVerificationToken(null);
        user.setEnabled(true);
        userRepository.save(user);
        logger.info("User with email {} successfully verified.", user.getEmail());
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
