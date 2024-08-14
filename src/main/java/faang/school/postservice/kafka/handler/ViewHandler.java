package faang.school.postservice.kafka.handler;

import faang.school.postservice.dto.event.PostViewEventDto;
import faang.school.postservice.dto.redis.PostRedisDto;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.repository.redis.RedisPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

import static faang.school.postservice.exception.message.PostOperationExceptionMessage.NO_POST_IN_CACHE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewHandler {

    private final RedisPostRepository postCache;

    private final ReentrantLock lock = new ReentrantLock();

    public void addViewToPost(PostViewEventDto postViewEventDto) {
        lock.lock();
        try {
            log.info("ViewHandler has been received PostViewEventDto.");
            PostRedisDto postRedisDto = postCache.findById(postViewEventDto.getPostId())
                    .orElseThrow(() -> new DataValidationException(NO_POST_IN_CACHE.getMessage()));

            long views = postRedisDto.getViews();
            views += 1;
            postRedisDto.setViews(views);

            postCache.save(postRedisDto);
            log.info("PostRedisDto has been saved with increment views.");
        } finally {
            lock.unlock();
        }
    }
}
