package net.danil.web.service;

import lombok.RequiredArgsConstructor;
import net.danil.web.domain.Solution;
import net.danil.web.repository.SolutionRepository;
import net.danil.web.repository.UserRepository;
import org.danil.model.Language;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TestRunnerSenderService {
    public static final String TOPIC_NAME = "test-topic";
    final private ReactiveKafkaProducerTemplate<String, TestMessage> kafka;
    final private SolutionRepository solutionRepository;
    final private UserRepository userRepository;

    public Mono<Long> send(Long userId, String code, String slug, Language language) {
        final var user = userRepository.getReferenceById(userId);
        final var solution = solutionRepository.save(new Solution(null, new Date(), user, code, slug, language, null));
        final var solutionId = solution.getId();
        return kafka.send(TOPIC_NAME, solutionId.toString(), new TestMessage(
                solutionId, solution.getCode(), solution.getProblemSlug(), solution.getLanguage()
        )).map(m -> solutionId);
    }
}
