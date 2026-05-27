package pl.edu.pk.projekt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.projekt.dto.LoginRequest;
import pl.edu.pk.projekt.dto.LoginResponse;
import pl.edu.pk.projekt.dto.UserRequest;
import pl.edu.pk.projekt.mapper.UserMapper;
import pl.edu.pk.projekt.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRequest req) {
        userService.createUser(UserMapper.toEntity(req));
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        String token = userService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}