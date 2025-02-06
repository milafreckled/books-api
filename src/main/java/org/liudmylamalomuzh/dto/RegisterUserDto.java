package org.liudmylamalomuzh.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUserDto {
    @Email(message = "Email should be valid")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    @NotEmpty
    private String fullName;

}
