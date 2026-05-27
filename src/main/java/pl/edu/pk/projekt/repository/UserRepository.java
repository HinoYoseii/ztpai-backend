package pl.edu.pk.projekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pk.projekt.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

