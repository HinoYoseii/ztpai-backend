package pl.edu.pk.projekt.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.projekt.dto.UpdateUserRequest;
import pl.edu.pk.projekt.dto.UserRequest;
import pl.edu.pk.projekt.dto.UserResponse;
import pl.edu.pk.projekt.mapper.UserMapper;
import pl.edu.pk.projekt.model.User;
import pl.edu.pk.projekt.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<String> createAdmin(@Valid @RequestBody UserRequest request) {
        User user = service.createAdmin(UserMapper.toEntity(request));
        return ResponseEntity.ok("Admin created");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public List<UserResponse> getAll() {
        return service.getAllUsers().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return UserMapper.toResponse(service.getUserById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        service.updateUser(id, UserMapper.toEntity(request));
        return UserMapper.toResponse(service.getUserById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public boolean deleteById(@PathVariable Long id) {
        return service.deleteUserById(id);
    }
}