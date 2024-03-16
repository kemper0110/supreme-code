package net.danil.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.danil.web.security.TokenInfo;
import net.danil.web.security.UserInfo;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
public class LoggedUser {
    UserInfo userInfo;
    TokenInfo tokenInfo;
}
