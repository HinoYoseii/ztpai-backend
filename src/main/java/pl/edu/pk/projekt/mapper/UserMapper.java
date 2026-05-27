package pl.edu.pk.projekt.mapper;

import pl.edu.pk.projekt.dto.UpdateUserRequest;
import pl.edu.pk.projekt.dto.UserRequest;
import pl.edu.pk.projekt.dto.UserResponse;
import pl.edu.pk.projekt.model.User;

public class UserMapper {
    public static User toEntity(UserRequest req){
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(req.getPassword());
        u.setEmail(req.getEmail());
        return u;
    }

    public static User toEntity(UpdateUserRequest req) {
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        return u;
    }

    public static UserResponse toResponse(User u){
        UserResponse res = new UserResponse();
        res.setId(u.getId());
        res.setUsername(u.getUsername());
        res.setEmail(u.getEmail());
        res.setRole(u.getRole());
        return res;
    }
}
