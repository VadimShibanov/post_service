package faang.school.postservice.kafka.handler;

import faang.school.postservice.dto.event.PostEventDto;
import faang.school.postservice.dto.redis.Feed;
import faang.school.postservice.repository.redis.RedisFeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedHandler {

    private final RedisFeedRepository feedCache;

    private final ReentrantLock lock = new ReentrantLock();

    @Value("${spring.data.redis.feed-cache.max-posts-amount}")
    private int maxPostsAmount;

    public void handleFeed(PostEventDto postEventDto) {
        postEventDto.getAuthorFollowersIds().forEach(followerId -> addPostToFeed(postEventDto, followerId));
    }

    public void addPostToFeed(PostEventDto postEventDto, Long followerId) {
        lock.lock();
        try {
            log.info("FeedHandler has been received PostEventDto and followerId.");

            Feed followerFeed = feedCache.findById(followerId)
                    .orElseGet(() -> Feed.builder()
                            .userId(followerId)
                            .postsIds(new ArrayList<>())
                            .build());

            addNewPost(postEventDto.getPostId(), followerFeed.getPostsIds());
            feedCache.save(followerFeed);
            log.info("Feed saved with post from PostEventDto.");
        } finally {
            lock.unlock();
        }
    }

    private void addNewPost(long postId, List<Long> postsIds) {
        if (postsIds.size() >= maxPostsAmount) {
            postsIds.remove(postsIds.iterator().next());
        }

        postsIds.add(postId);
    }
}
