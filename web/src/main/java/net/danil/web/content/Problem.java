package net.danil.web.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.danil.model.Language;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    private String problemSlug;
    private String name;
    private String description;
    private Difficulty difficulty;
    private Language[] languages;

    public enum Difficulty {
        Easy, Normal, Hard
    }
}
