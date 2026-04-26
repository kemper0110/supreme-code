package org.supremecode.web.user.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo implements Principal {
    public Long id;
    public String username;

    @Override
    public String getName() {
        return username;
    }
}
