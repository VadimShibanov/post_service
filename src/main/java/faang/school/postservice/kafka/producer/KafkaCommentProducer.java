package faang.school.postservice.kafka.producer;

import faang.school.postservice.dto.event.CommentEventDto;
import faang.school.postservice.dto.event.EventDto;
import faang.school.postservice.model.Comment;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaCommentProducer extends AbstractEventProducer {

    public KafkaCommentProducer(
            KafkaTemplate<String, EventDto> kafkaTemplate,
            NewTopic commentKafkaTopic
    ) {
        super(kafkaTemplate, commentKafkaTopic);
    }

    @Async
    @Retryable
    public void sendCommentEvent(Comment comment) {
        CommentEventDto commentEventDto = CommentEventDto.builder()
                .commentAuthorId(comment.getAuthorId())
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();

        sendEvent(commentEventDto, String.valueOf(comment.getPost().getAuthorId()));
        log.info("CommentEventDto has been sent from KafkaCommentProducer");
    }
}
