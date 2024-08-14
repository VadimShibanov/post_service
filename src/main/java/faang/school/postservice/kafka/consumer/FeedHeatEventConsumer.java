package faang.school.postservice.kafka.consumer;

import faang.school.postservice.dto.event.FeedHeatEventDto;
import faang.school.postservice.kafka.handler.FeedHeatHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedHeatEventConsumer {

    private final FeedHeatHandler feedHeatHandler;

    @KafkaListener(topics = "${spring.kafka.topics-names.feed-heat}", groupId = "spring.kafka.group-id")
    public void handleFeedHeat(FeedHeatEventDto feedHeatEventDto, Acknowledgment ack) {
        log.info("FeedHeatEventConsumer has been received FeedHeatEventDto.");
        feedHeatHandler.addFeedToUser(feedHeatEventDto);

        ack.acknowledge();
    }
}
