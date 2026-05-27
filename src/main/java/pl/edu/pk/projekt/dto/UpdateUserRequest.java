package pl.edu.pk.projekt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @NotNull(message = "Username is required")
    @Size(min = 3, max = 30)
    private String username;

    @NotNull(message = "Email address is required")
    private String email;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}