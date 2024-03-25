package net.danil.web.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("users")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String image;
    private List<Solution> solutions;
}
