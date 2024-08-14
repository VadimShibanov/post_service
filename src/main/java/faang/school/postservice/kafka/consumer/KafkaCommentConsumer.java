package faang.school.postservice.kafka.consumer;

import faang.school.postservice.dto.event.CommentEventDto;
import faang.school.postservice.kafka.handler.CommentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaCommentConsumer {

    private final CommentHandler commentHandler;

    @KafkaListener(topics = "${spring.kafka.topics-names.comment}", groupId = "${spring.kafka.group-id}")
    public void listenCommentsEvents(CommentEventDto commentEventDto, Acknowledgment ack) {
        log.info("KafkaCommentConsumer has been received CommentEventDto.");
        commentHandler.addCommentToPost(commentEventDto);

        ack.acknowledge();
    }
}
