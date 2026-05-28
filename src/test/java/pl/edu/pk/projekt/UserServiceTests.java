package pl.edu.pk.projekt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.pk.projekt.model.Role;
import pl.edu.pk.projekt.model.User;
import pl.edu.pk.projekt.repository.UserRepository;
import pl.edu.pk.projekt.security.JwtUtil;
import pl.edu.pk.projekt.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks
    UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("john");
        sampleUser.setPassword("raw_password");
        sampleUser.setEmail("john@example.com");
        sampleUser.setRole(Role.USER);
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_setsRoleUserAndEncodesPassword() {
        User input = new User();
        input.setUsername("john");
        input.setPassword("raw_password");

        when(passwordEncoder.encode("raw_password")).thenReturn("encoded_password");
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(input);

        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getPassword()).isEqualTo("encoded_password");
        verify(repository).save(input);
    }

    @Test
    void createUser_persistsAndReturnsUser() {
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(repository.save(any())).thenReturn(sampleUser);

        User result = userService.createUser(sampleUser);

        assertThat(result).isSameAs(sampleUser);
    }

    @Test
    void createUser_throwsConflict_whenUsernameAlreadyExists() {
        User input = new User();
        input.setUsername("john");
        input.setEmail("different@example.com");
        input.setPassword("password123");

        when(repository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo("Username 'john' is already taken");
                });

        verify(repository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void createUser_throwsConflict_whenEmailAlreadyExists() {
        User input = new User();
        input.setUsername("different_user");
        input.setEmail("john@example.com");
        input.setPassword("password123");

        when(repository.existsByUsername("different_user")).thenReturn(false);
        when(repository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo("Email 'john@example.com' is already registered");
                });

        verify(repository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }


    // ── createAdmin ───────────────────────────────────────────────────────────

    @Test
    void createAdmin_setsRoleAdminAndEncodesPassword() {
        User input = new User();
        input.setUsername("alice");
        input.setPassword("secret");

        when(passwordEncoder.encode("secret")).thenReturn("encoded_secret");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createAdmin(input);

        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.getPassword()).isEqualTo("encoded_secret");
    }

    @Test
    void createAdmin_throwsConflict_whenUsernameAlreadyExists() {
        User input = new User();
        input.setUsername("admin_user");
        input.setEmail("admin@example.com");
        input.setPassword("admin123");

        when(repository.existsByUsername("admin_user")).thenReturn(true);

        assertThatThrownBy(() -> userService.createAdmin(input))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo("Username 'admin_user' is already taken");
                });

        verify(repository, never()).save(any());
    }

    @Test
    void createAdmin_throwsConflict_whenEmailAlreadyExists() {
        User input = new User();
        input.setUsername("new_admin");
        input.setEmail("existing@example.com");
        input.setPassword("admin123");

        when(repository.existsByUsername("new_admin")).thenReturn(false);
        when(repository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createAdmin(input))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo("Email 'existing@example.com' is already registered");
                });

        verify(repository, never()).save(any());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        when(repository.findByUsername("john")).thenReturn(Optional.of(sampleUser));
        when(jwtUtil.generateToken("john", "USER")).thenReturn("jwt_token");

        String token = userService.login("john", "raw_password");

        assertThat(token).isEqualTo("jwt_token");
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("john", "raw_password"));
    }

    @Test
    void login_throwsBadCredentials_whenAuthFails() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> userService.login("john", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_throwsNotFound_whenUserMissingAfterAuth() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("ghost", "pass"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_returnsUser_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(sampleUser);
    }

    @Test
    void getUserById_throwsNotFound_whenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── getUserByUsername ─────────────────────────────────────────────────────

    @Test
    void getUserByUsername_returnsUser_whenFound() {
        when(repository.findByUsername("john")).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserByUsername("john");

        assertThat(result.getUsername()).isEqualTo("john");
    }

    @Test
    void getUserByUsername_throwsNotFound_whenMissing() {
        when(repository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("nobody"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsAllUsers() {
        when(repository.findAll()).thenReturn(List.of(sampleUser));

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(1).containsExactly(sampleUser);
    }

    @Test
    void getAllUsers_returnsEmptyList_whenNoUsers() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(userService.getAllUsers()).isEmpty();
    }

    // ── deleteUserById ────────────────────────────────────────────────────────

    @Test
    void deleteUserById_returnsTrue_andDeletes_whenUserExists() {
        when(repository.existsById(1L)).thenReturn(true);

        boolean result = userService.deleteUserById(1L);

        assertThat(result).isTrue();
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteUserById_returnsFalse_andSkipsDelete_whenUserMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        boolean result = userService.deleteUserById(99L);

        assertThat(result).isFalse();
        verify(repository, never()).deleteById(any());
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_updatesFieldsAndSaves_whenUserExists() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("old");
        existing.setEmail("old@example.com");
        existing.setPassword("old_hash");

        User update = new User();
        update.setUsername("new");
        update.setEmail("new@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(1L, update);

        assertThat(existing.getUsername()).isEqualTo("new");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getPassword()).isEqualTo("old_hash");

        verify(repository).save(existing);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_throwsNotFound_whenUserMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new User()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateUser_throwsConflict_whenNewUsernameAlreadyExistsForDifferentUser() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("old_user");
        existing.setEmail("old@example.com");
        existing.setPassword("old_hash");

        User update = new User();
        update.setUsername("john");
        update.setEmail("new@example.com");
        update.setPassword("new_password");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, update))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo("Username 'john' is already taken");
                });

        verify(repository, never()).save(any());
    }

    @Test
    void updateUser_allowsSameUsername_whenUpdatingOwnUser() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("john");
        existing.setEmail("old@example.com");
        existing.setPassword("old_hash");

        User update = new User();
        update.setUsername("john");
        update.setEmail("new@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("new@example.com")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(1L, update);

        assertThat(existing.getUsername()).isEqualTo("john");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        verify(repository).save(existing);
        verify(repository, never()).existsByUsername(anyString());
    }

    @Test
    void updateUser_throwsConflict_whenNewEmailAlreadyExistsForDifferentUser() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("user1");
        existing.setEmail("user1@example.com");
        existing.setPassword("hash1");

        User update = new User();
        update.setUsername("user1_updated");
        update.setEmail("john@example.com");
        update.setPassword("new_password");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByUsername("user1_updated")).thenReturn(false);
        when(repository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, update))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo("Email 'john@example.com' is already registered");
                });

        verify(repository, never()).save(any());
    }

    @Test
    void updateUser_allowsSameEmail_whenUpdatingOwnUser() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("john");
        existing.setEmail("john@example.com");
        existing.setPassword("old_hash");

        User update = new User();
        update.setUsername("john_new");
        update.setEmail("john@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByUsername("john_new")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(1L, update);

        assertThat(existing.getUsername()).isEqualTo("john_new");
        assertThat(existing.getEmail()).isEqualTo("john@example.com");
        verify(repository).save(existing);
        verify(repository, never()).existsByEmail(anyString());
    }
}