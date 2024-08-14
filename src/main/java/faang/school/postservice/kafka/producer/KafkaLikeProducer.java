package faang.school.postservice.kafka.producer;

import faang.school.postservice.dto.event.EventDto;
import faang.school.postservice.dto.event.LikeEventDto;
import faang.school.postservice.model.Like;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaLikeProducer extends AbstractEventProducer {

    public KafkaLikeProducer(
            KafkaTemplate<String, EventDto> kafkaTemplate,
            NewTopic likeKafkaTopic
    ) {
        super(kafkaTemplate, likeKafkaTopic);
    }

    @Async
    @Retryable
    public void sendLikeEvent(Like like) {
        LikeEventDto likeEventDto = LikeEventDto.builder()
                .postId(like.getPost().getId())
                .build();

        sendEvent(likeEventDto, String.valueOf(like.getPost().getAuthorId()));
        log.info("LikeEventDto has been sent from KafkaLikeProducer");
    }
}
