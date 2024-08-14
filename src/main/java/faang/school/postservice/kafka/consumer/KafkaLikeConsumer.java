package faang.school.postservice.kafka.consumer;

import faang.school.postservice.dto.event.LikeEventDto;
import faang.school.postservice.kafka.handler.LikeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLikeConsumer {

    private final LikeHandler likeHandler;

    @KafkaListener(topics = "${spring.kafka.topics-names.like}", groupId = "${spring.kafka.group-id}")
    public void listenLikesEvents(LikeEventDto likeEventDto, Acknowledgment ack) {
        log.info("KafkaLikeConsumer has been received LikeEventDto.");
        likeHandler.addLikeToPost(likeEventDto);

        ack.acknowledge();
    }
}
