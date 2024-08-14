package faang.school.postservice.service.feed;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.event.FeedHeatEventDto;
import faang.school.postservice.dto.post.PostDto;
import faang.school.postservice.dto.redis.Feed;
import faang.school.postservice.dto.redis.PostRedisDto;
import faang.school.postservice.dto.redis.UserRedisDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.kafka.producer.KafkaFeedHeatEventProducer;
import faang.school.postservice.repository.redis.RedisFeedRepository;
import faang.school.postservice.repository.redis.RedisPostRepository;
import faang.school.postservice.repository.redis.RedisUserRepository;
import faang.school.postservice.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final UserServiceClient userServiceClient;

    private final ListSplitter listSplitter;

    private final KafkaFeedHeatEventProducer feedHeatEventProducer;

    private final RedisFeedRepository feedCache;

    private final RedisPostRepository postCache;

    private final RedisUserRepository userCache;

    private final PostService postService;

    @Value("${post.feed.get-batch-size}")
    private int feedBatchSize;

    @Value("${spring.data.redis.feed-cache.heater-batch-size}")
    public int heaterBatchSize;

    public List<PostRedisDto> getFeed(Long userId, Long lastViewedPostId) {
        Optional<Feed> userFeed = feedCache.findById(userId);
        Optional<List<PostRedisDto>> posts;

        if (lastViewedPostId == null) {
            posts = Optional.of(userFeed.get().getPostsIds().stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(feedBatchSize)
                    .map(this::getPostDto)
                    .toList());
        } else {
            List<Long> cachedIds = userFeed.get().getPostsIds().stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();

            int lastViewedPostIndex = cachedIds.indexOf(lastViewedPostId);

            posts = Optional.of(cachedIds.stream()
                    .skip(lastViewedPostIndex + 1)
                    .limit(feedBatchSize)
                    .map(this::getPostDto)
                    .toList());
        }

        return collectUserFeed(posts.get());
    }

    public void heat() {
        List<List<Long>> batchedUserIds = listSplitter.splitList(userServiceClient.getAllUsersIds(), heaterBatchSize);

        batchedUserIds.stream()
                .map(FeedHeatEventDto::new)
                .forEach(feedHeatEventProducer::sendFeedHeatEvent);
    }

    private PostRedisDto getPostDto(Long postId) {
        if (postCache.findById(postId).isPresent()) {
            PostRedisDto postRedisDto = postCache.findById(postId).get();
            long views = postRedisDto.getViews();
            views = views + 1;
            postRedisDto.setViews(views);
            postCache.save(postRedisDto);
            return postRedisDto;
        } else {
            PostDto post = postService.getPostById(postId);
            return PostRedisDto.builder()
                    .id(postId)
                    .authorId(post.getAuthorId())
                    .content(post.getContent())
                    .publishedAt(post.getPublishedAt())
                    .likes(0L)
                    .views(1L)
                    .comments(new LinkedHashSet<>())
                    .build();
        }
    }

    private List<PostRedisDto> collectUserFeed(List<PostRedisDto> fullPostsBatch) {
        return fullPostsBatch.stream()
                .peek(postRedisDto -> postRedisDto.setAuthor(getUserRedisDto(postRedisDto.getAuthorId())))
                .toList();
    }

    private UserRedisDto getUserRedisDto(Long userId) {
        Optional<UserRedisDto> userFromCache = userCache.findById(userId);

        return userFromCache.orElseGet(() -> {
            UserDto userDto = userServiceClient.getUser(userId);
            UserRedisDto userFromDb = new UserRedisDto();
            userFromDb.setUsername(userDto.getUsername());
            return userFromDb;
        });
    }
}
