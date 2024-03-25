package net.danil.web.repository;

import net.danil.web.dto.BasicUserProjection;
import net.danil.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    @Query("select u from users u")
    Flux<BasicUserProjection> findAllPreview();

    Mono<User> findByUsername(String username);
}
