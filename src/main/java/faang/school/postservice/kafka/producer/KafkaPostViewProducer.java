package faang.school.postservice.kafka.producer;

import faang.school.postservice.dto.event.EventDto;
import faang.school.postservice.dto.event.PostViewEventDto;
import faang.school.postservice.model.Post;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaPostViewProducer extends AbstractEventProducer {

    public KafkaPostViewProducer(
            KafkaTemplate<String, EventDto> kafkaTemplate,
            NewTopic postViewKafkaTopic
    ) {
        super(kafkaTemplate, postViewKafkaTopic);
    }

    @Async
    @Retryable
    public void sendPostViewEvent(Post post) {
        PostViewEventDto postViewEventDto = new PostViewEventDto();
        postViewEventDto.setPostId(post.getId());

        sendEvent(postViewEventDto, String.valueOf(post.getAuthorId()));
        log.info("PostViewEventDto has been sent from KafkaPostViewProducer");
    }
}
