package net.danil.web.dto;

/**
 * Projection for {@link net.danil.web.model.User}
 */
public interface BasicUserProjection {
    Long getId();

    String getUsername();

    String getImage();
}