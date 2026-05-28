package pl.edu.pk.projekt.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.pk.projekt.model.Role;
import pl.edu.pk.projekt.model.User;
import pl.edu.pk.projekt.repository.UserRepository;
import pl.edu.pk.projekt.security.JwtUtil;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User createUser(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username '" + user.getUsername() + "' is already taken");
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email '" + user.getEmail() + "' is already registered");
        }

        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public User createAdmin(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username '" + user.getUsername() + "' is already taken");
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email '" + user.getEmail() + "' is already registered");
        }

        user.setRole(Role.ADMIN);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public String login(String username, String password) {
        System.out.print("test");
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        User user = getUserByUsername(username);
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public User getUserByUsername(String username) {
        return repository.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User with username " + username + " doesn't exist."));
    }

    public User getUserById(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist"));
    }

    public boolean deleteUserById(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public void updateUser(Long id, User newUser) {
        repository.findById(id)
                .map(user -> {
                    if (!newUser.getUsername().equals(user.getUsername()) &&
                            repository.existsByUsername(newUser.getUsername())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Username '" + newUser.getUsername() + "' is already taken");
                    }
                    if (!newUser.getEmail().equals(user.getEmail()) &&
                            repository.existsByEmail(newUser.getEmail())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Email '" + newUser.getEmail() + "' is already registered");
                    }
                    user.setUsername(newUser.getUsername());
                    user.setEmail(newUser.getEmail());
                    return repository.save(user);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wybrany użytkownik nie istnieje"));
    }
}