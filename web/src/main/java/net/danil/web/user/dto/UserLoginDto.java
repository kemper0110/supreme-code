package net.danil.web.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginDto(
        @NotBlank
        @Size(min = 4, max = 20)
        String username,
        @NotBlank
        @Size(min = 3, max = 30)
        String password
) {
}
