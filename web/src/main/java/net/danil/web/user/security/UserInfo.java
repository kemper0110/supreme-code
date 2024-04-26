package net.danil.web.user.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo implements Principal {
    private Long id;
    private String username;

    @Override
    public String getName() {
        return username;
    }
}
