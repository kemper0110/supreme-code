package net.danil.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskRunnerChannelService {
    private final ConcurrentHashMap<String, MessageHandler> messageHandlers =
            new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(TaskRunnerChannelService.class);

    @KafkaListener(topics = "result-topic")
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
        handler.handleMessage(new GenericMessage<>(in));
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
