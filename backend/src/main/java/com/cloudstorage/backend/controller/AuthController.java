package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.AuthRequest;
import com.cloudstorage.backend.dto.AuthResponse;
import com.cloudstorage.backend.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000") // 🔥 REQUIRED
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {
        System.out.println("REGISTER CONTROLLER HIT: " + request.getEmail());
        return new AuthResponse(
                authService.register(request.getEmail(), request.getPassword())
        );
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return new AuthResponse(
                authService.login(request.getEmail(), request.getPassword())
        );
    }

    @GetMapping("/me")
    public String me(Authentication authentication) {
        return authentication.getName();
    }
}
