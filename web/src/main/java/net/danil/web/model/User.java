package net.danil.web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Table(name = "users")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(min = 5, max = 30)
    private String username;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 50)
    private String password;

    private String image;
}
