package net.danil.web.service;

import lombok.RequiredArgsConstructor;
import net.danil.web.dto.TestMessage;
import net.danil.web.model.Solution;
import net.danil.web.repository.SolutionRepository;
import net.danil.web.repository.UserRepository;
import org.danil.model.Language;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestRunnerSenderService {
    public static final String TOPIC_NAME = "test-topic";
    final private KafkaTemplate<String, TestMessage> kafka;
    final private SolutionRepository solutionRepository;
    final private UserRepository userRepository;

    public Long send(Long userId, String code, String slug, Language language) {
        final var user = userRepository.getReferenceById(userId);
        final var solution = solutionRepository.save(new Solution(null, user, code, slug, language, null));
        final var solutionId = solution.getId();
        kafka.send(TOPIC_NAME, solutionId.toString(), new TestMessage(
                solutionId, solution.getCode(), solution.getProblemSlug(), solution.getLanguage()
        ));

        return solutionId;
    }
}
