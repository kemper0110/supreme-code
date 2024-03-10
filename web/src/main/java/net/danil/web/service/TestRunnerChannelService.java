package net.danil.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.danil.web.controller.ProblemController;
import net.danil.web.model.SolutionResult;
import net.danil.web.repository.SolutionRepository;
import net.danil.web.repository.SolutionResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TestRunnerChannelService {
    public static final String TOPIC_NAME = "test-result-topic";

    private final ConcurrentHashMap<String, MessageHandler> messageHandlers =
            new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(TestRunnerChannelService.class);
    private final SolutionResultRepository solutionResultRepository;
    private final SolutionRepository solutionRepository;
    final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = TOPIC_NAME)
    protected void listen(@Payload String in, @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageId) {
        if (messageId == null) {
            logger.error("received null message id");
            return;
        }
        logger.info("received result forId({}): {}", messageId, in);
        final var handler = messageHandlers.get(messageId);
        if (handler == null) {
            logger.error("null handler detected forId({})", messageId);
            return;
        }
        try {
            final var testResult = mapper.readValue(in, ProblemController.TestResult.class);
            final var solution = solutionRepository.findById(testResult.solutionId()).get();

            solutionResultRepository.save(
              new SolutionResult(solution.getId(), testResult.tests(), testResult.failures(), testResult.errors(), testResult.statusCode(), testResult.time(),
                      testResult.logs(), testResult.xml(), solution)
            );
            handler.handleMessage(new GenericMessage<>(testResult));
        } catch (JsonProcessingException e) {
            handler.handleMessage(new GenericMessage<>(in, Map.of("exception", e)));
            throw new RuntimeException(e);
        }
    }

    public void subscribe(String id, MessageHandler channel) {
        logger.info("subscribed handler for " + id);
        messageHandlers.put(id, channel);
    }

    public void unsubscribe(String id) {
        logger.info("unsubscribed handler for " + id);
        messageHandlers.remove(id);
    }
}
