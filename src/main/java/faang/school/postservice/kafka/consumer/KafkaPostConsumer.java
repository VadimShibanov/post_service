package faang.school.postservice.kafka.consumer;

import faang.school.postservice.dto.event.PostEventDto;
import faang.school.postservice.kafka.handler.FeedHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPostConsumer {

    private final FeedHandler feedHandler;

    @KafkaListener(topics = "${spring.kafka.topics-names.post}", groupId = "${spring.kafka.group-id}")
    public void listenPostsEvents(PostEventDto postEventDto, Acknowledgment ack) {
        log.info("KafkaPostConsumer has been received PostEventDto.");
        feedHandler.handleFeed(postEventDto);

        ack.acknowledge();
    }
}
