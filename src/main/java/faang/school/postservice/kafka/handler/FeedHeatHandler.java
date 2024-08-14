package faang.school.postservice.kafka.handler;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.comment.CommentDto;
import faang.school.postservice.dto.event.FeedHeatEventDto;
import faang.school.postservice.dto.redis.Feed;
import faang.school.postservice.dto.redis.PostRedisDto;
import faang.school.postservice.dto.redis.UserRedisDto;
import faang.school.postservice.mapper.CommentMapper;
import faang.school.postservice.mapper.PostMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.repository.LikeRepository;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.repository.redis.RedisFeedRepository;
import faang.school.postservice.repository.redis.RedisPostRepository;
import faang.school.postservice.repository.redis.RedisUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeedHeatHandler {

    private final UserServiceClient userServiceClient;

    private final PostRepository postRepository;

    private final CommentRepository commentRepository;

    private final LikeRepository likeRepository;

    private final PostMapper postMapper;

    private final CommentMapper commentMapper;

    private final RedisFeedRepository feedCache;

    private final RedisUserRepository userCache;

    private final RedisPostRepository postCache;

    private final UserContext userContext;

    @Value("${spring.data.redis.post-cache.ttl}")
    private int timeToLive;

    @Value("${post.feed.get-batch-for-heat-feed-size}")
    public int batchSizeForHeatFeed;

    @Value("${spring.data.redis.comments-amount}")
    private int maxComments;

    @Transactional(readOnly = true)
    public void addFeedToUser(FeedHeatEventDto feedHeatEventDto) {
        feedHeatEventDto.getUsersIds().forEach(this::heatFeedForUser);
    }

    private void heatFeedForUser(Long userId) {
        userContext.setUserId(userId);

        List<PostRedisDto> posts = getPosts(userId);

        List<UserRedisDto> authors = getAuthors(posts);

        List<Long> usersFeedPostsIds = posts.stream()
                .map(PostRedisDto::getId)
                .toList();

        Feed feed = Feed.builder()
                .userId(userId)
                .postsIds(usersFeedPostsIds)
                .build();

        feedCache.save(feed);
        userCache.saveAll(authors);
        postCache.saveAll(posts);
    }

    private List<PostRedisDto> getPosts(Long userId) {
        return postMapper.toDtoRedis(
                        userServiceClient.getFollowings(userId).stream()
                                .flatMap(userDto -> postRepository.findByAuthorId(userDto.getId()).stream())
                                .limit(batchSizeForHeatFeed)
                                .toList()
                ).stream()
                .peek(postRedisDto -> {
                    postRedisDto.setTtl(timeToLive);
                    Set<CommentDto> comments = commentMapper.toDtoSet(commentRepository.findAllByPostId(postRedisDto.getId()).stream()
                            .sorted(Comparator.comparing(Comment::getId).reversed())
                            .limit(maxComments)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                    postRedisDto.setComments(comments);
                }).peek(postRedisDto -> {
                    long likes = likeRepository.findByPostId(postRedisDto.getId());
                    postRedisDto.setLikes(likes);
                }).toList();
    }

    private List<UserRedisDto> getAuthors(List<PostRedisDto> posts) {
        return posts.stream()
                .map(PostRedisDto::getAuthorId)
                .map(userServiceClient::getUser)
                .map(userDto -> {
                    UserRedisDto userRedisDto = new UserRedisDto();
                    userRedisDto.setId(userDto.getId());
                    userRedisDto.setUsername(userDto.getUsername());
                    userRedisDto.setTtl(timeToLive);
                    return userRedisDto;
                })
                .toList();
    }
}
