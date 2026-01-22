package com.cloudstorage.backend.service;

import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.UserRepository;
import com.cloudstorage.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(String email, String password) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider("LOCAL"); // 🔥 FIX
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return jwtUtil.generateToken(email);
    }

    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(email);
    }
}
