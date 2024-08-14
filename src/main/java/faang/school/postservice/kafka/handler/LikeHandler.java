package faang.school.postservice.kafka.handler;

import faang.school.postservice.dto.event.LikeEventDto;
import faang.school.postservice.dto.redis.PostRedisDto;
import faang.school.postservice.repository.redis.RedisPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeHandler {

    private final RedisPostRepository postCache;

    private final ReentrantLock lock = new ReentrantLock();

    public void addLikeToPost(LikeEventDto likeEventDto) {
        lock.lock();
        try {
            log.info("LikeHandler has been received LikeEventDto.");
            PostRedisDto postRedisDto = postCache.findById(likeEventDto.getPostId()).get();

            long likes = postRedisDto.getLikes();
            likes += 1;
            postRedisDto.setLikes(likes);

            postCache.save(postRedisDto);
            log.info("LikeEventDto saved with increment likes.");
        } finally {
            lock.unlock();
        }
    }
}
