package net.danil.web.repository;

import net.danil.web.dto.BasicUserProjection;
import net.danil.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from users u")
    List<BasicUserProjection> findAllPreview();

    User findByUsername(String username);
}
