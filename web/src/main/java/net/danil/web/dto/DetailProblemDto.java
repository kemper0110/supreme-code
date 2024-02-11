package net.danil.web.dto;

import net.danil.web.model.Problem;

import java.util.List;

public record DetailProblemDto(Long id, String name, Boolean active, String description,
                               Problem.Difficulty difficulty,
                               List<BasicProblemLanguageDto> languages) {
    public static DetailProblemDto fromProblem(Problem problem) {
        return new DetailProblemDto(problem.getId(), problem.getName(), problem.getActive(), problem.getDescription(),
                problem.getDifficulty(),
                problem.getLanguages().stream().map(BasicProblemLanguageDto::fromProblemLanguage).toList()
        );
    }
}

