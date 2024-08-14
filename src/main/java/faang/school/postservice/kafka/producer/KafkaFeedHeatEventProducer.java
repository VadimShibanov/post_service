package faang.school.postservice.kafka.producer;

import faang.school.postservice.dto.event.EventDto;
import faang.school.postservice.dto.event.FeedHeatEventDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaFeedHeatEventProducer extends AbstractEventProducer {

    public KafkaFeedHeatEventProducer(
            KafkaTemplate<String, EventDto> kafkaTemplate,
            NewTopic feedHeatKafkaTopic
    ) {
        super(kafkaTemplate, feedHeatKafkaTopic);
    }

    public void sendFeedHeatEvent(FeedHeatEventDto heatEvent) {
        sendEvent(heatEvent, String.valueOf(heatEvent.getUsersIds()));
        log.info("FeedHeatEventDto has been sent from KafkaFeedHeatEventProducer");
    }
}
