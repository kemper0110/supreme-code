package org.supremecode.web.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.supremecode.web.user.security.TokenInfo;
import org.supremecode.web.user.security.UserInfo;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
public class LoggedUser {
    UserInfo userInfo;
    TokenInfo tokenInfo;
}
