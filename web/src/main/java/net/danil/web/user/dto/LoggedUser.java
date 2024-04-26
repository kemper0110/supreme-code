package net.danil.web.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.danil.web.user.security.TokenInfo;
import net.danil.web.user.security.UserInfo;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
public class LoggedUser {
    UserInfo userInfo;
    TokenInfo tokenInfo;
}
