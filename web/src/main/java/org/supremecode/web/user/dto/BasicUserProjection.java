package org.supremecode.web.user.dto;

import org.supremecode.web.domain.User;

/**
 * Projection for {@link User}
 */
public interface BasicUserProjection {
    Long getId();

    String getUsername();

    String getImage();
}