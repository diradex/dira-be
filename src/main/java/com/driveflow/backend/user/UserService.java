package com.driveflow.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getInstructors() {
        return userRepository.findByRole(Role.INSTRUCTOR);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User updateUser(Long id, User updated) {
        User user = getUserById(id);
        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());
        user.setPhoneNumber(updated.getPhoneNumber());
        return userRepository.save(user);
    }
}
