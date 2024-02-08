package net.danil.web.controller;

import lombok.RequiredArgsConstructor;
import net.danil.web.model.Problem;
import net.danil.web.repository.ProblemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem")
public class ProblemController {
    private final ProblemRepository problemRepository;

    @GetMapping
    List<Problem> index() {
        return problemRepository.findAll();
    }
}
