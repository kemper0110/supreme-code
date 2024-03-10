package net.danil.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.danil.web.model.Solution;
import net.danil.web.repository.SolutionRepository;
import net.danil.web.repository.UserRepository;
import org.danil.model.Language;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestRunnerSenderService {
    public static final String TOPIC_NAME = "test-topic";
    final private KafkaTemplate<String, String> kafka;
    final private SolutionRepository solutionRepository;
    final private UserRepository userRepository;
    final private ObjectMapper mapper = new ObjectMapper();


    public record TestMessage(Long solutionId, String code, String testSlug, Language language) {
    }

    public String send(Long userId, String code, String slug, Language language) {
        try {
            final var taskId = UUID.randomUUID().toString();

            final var user = userRepository.getReferenceById(userId);
            final var solution = solutionRepository.save(new Solution(null, user, code, slug, language, null));
            final var solutionId = solution.getId();
            kafka.send(TOPIC_NAME, taskId, mapper.writeValueAsString(new TestMessage(
                    solutionId, solution.getCode(), solution.getProblemSlug(), solution.getLanguage()
            )));

            return taskId;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
