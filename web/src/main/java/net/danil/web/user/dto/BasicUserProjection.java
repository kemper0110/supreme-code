package net.danil.web.user.dto;

import net.danil.web.user.model.User;

/**
 * Projection for {@link User}
 */
public interface BasicUserProjection {
    Long getId();

    String getUsername();

    String getImage();
}