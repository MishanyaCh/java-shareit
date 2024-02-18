package ru.practicum.shareit.user.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
public class UserDto {
    private int id;
    private String name;

    @NotNull
    @Email
    private String email;

    public UserDto(int idArg, String nameArg, String emailArg) {
        id = idArg;
        name = nameArg;
        email = emailArg;
    }

    @Override
    public String toString() {
        return "UserDto{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email +  '\'' + "}" + '\n';
    }
}
