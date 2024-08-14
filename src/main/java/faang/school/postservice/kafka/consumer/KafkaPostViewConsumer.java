package faang.school.postservice.kafka.consumer;

import faang.school.postservice.dto.event.PostViewEventDto;
import faang.school.postservice.kafka.handler.ViewHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPostViewConsumer {

    private final ViewHandler viewHandler;

    @KafkaListener(topics = "${spring.kafka.topics-names.view}", groupId = "${spring.kafka.group-id}")
    public void listenPostViewsEvents(PostViewEventDto postViewEventDto, Acknowledgment ack) {
        log.info("KafkaPostConsumer has been received PostEventDto.");
        viewHandler.addViewToPost(postViewEventDto);

        ack.acknowledge();
    }
}
