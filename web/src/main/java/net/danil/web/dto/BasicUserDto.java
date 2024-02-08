package net.danil.web.dto;

import net.danil.web.model.User;

public record BasicUserDto(Long id, String username, String image) {
    public static BasicUserDto fromUser(User user) {
        return new BasicUserDto(user.getId(), user.getUsername(), user.getImage());
    }
}
