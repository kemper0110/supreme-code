package org.danil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    private String id;
    private String name;
    private String description;
    private Difficulty difficulty;
    private List<Language> languages;
    private List<String> tags;

    public enum Difficulty {
        Easy, Normal, Hard
    }
}
