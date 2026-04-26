package org.supremecode.web.repository;

import org.supremecode.web.user.dto.BasicUserProjection;
import org.supremecode.web.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u")
    List<BasicUserProjection> findAllPreview();

    User findByUsername(String username);
}
