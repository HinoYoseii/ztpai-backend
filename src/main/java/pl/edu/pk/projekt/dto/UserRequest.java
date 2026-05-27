package pl.edu.pk.projekt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.edu.pk.projekt.model.Role;

public class UserRequest {

    @NotNull(message = "Username is required")
    @Size(min = 3, max = 30)
    private String username;

    @NotNull(message = "Password is required")
    @Size(min = 6)
    private String password;
    @NotNull(message = "Email address is required")
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
