package faang.school.postservice.service.post;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.post.PostDto;
import faang.school.postservice.dto.redis.PostRedisDto;
import faang.school.postservice.dto.redis.UserRedisDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.exception.DataOperationException;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.kafka.producer.KafkaPostProducer;
import faang.school.postservice.kafka.producer.KafkaPostViewProducer;
import faang.school.postservice.mapper.PostMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.repository.redis.RedisPostRepository;
import faang.school.postservice.repository.redis.RedisUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static faang.school.postservice.exception.message.PostOperationExceptionMessage.RE_DELETING_POST_EXCEPTION;
import static faang.school.postservice.exception.message.PostOperationExceptionMessage.RE_PUBLISHING_POST_EXCEPTION;
import static faang.school.postservice.exception.message.PostValidationExceptionMessage.NON_EXISTING_POST_EXCEPTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    private final PostMapper postMapper;

    private final PostVerifier postVerifier;

    private final RedisPostRepository postCache;

    private final RedisUserRepository userCache;

    private final KafkaPostProducer kafkaPostProducer;

    private final KafkaPostViewProducer kafkaPostViewProducer;

    private final UserServiceClient userServiceClient;

    @Value("${spring.data.redis.post-cache.ttl}")
    private int timeToLive;

    public PostDto createPost(@Valid PostDto postDto) {
        postVerifier.verifyAuthorExistence(postDto.getAuthorId(), postDto.getProjectId());

        Post postDraft = postMapper.toEntity(postDto);
        postDraft.setPublished(false);
        postDraft.setDeleted(false);

        log.info("Post has been created.");
        return postMapper.toDto(postRepository.save(postDraft));
    }

    @Transactional
    public PostDto publishPost(long postId) {
        Post postToBePublished = getPost(postId);

        if (postToBePublished.isPublished()) {
            throw new DataOperationException(RE_PUBLISHING_POST_EXCEPTION.getMessage());
        }

        postToBePublished.setPublished(true);
        postToBePublished.setPublishedAt(LocalDateTime.now());

        Post publishedPost = postRepository.save(postToBePublished);
        log.info("Post has been published.");

        kafkaPostProducer.sendPostEvent(publishedPost);
        log.info("PostEvent has been sent from PostService.");

        cachePost(postMapper.toDtoRedis(publishedPost));
        cacheAuthor(publishedPost.getAuthorId());
        log.info("Post and author have been cached.");

        return postMapper.toDto(postRepository.save(postToBePublished));
    }

    public PostDto updatePost(PostDto postDto) {
        postVerifier.verifyAuthorExistence(postDto.getAuthorId(), postDto.getProjectId());

        Post postToBeUpdated = getPost(postDto.getId());

        postVerifier.verifyPostMatchingSystem(postDto, postToBeUpdated);

        postToBeUpdated.setContent(postDto.getContent());
        return postMapper.toDto(postRepository.save(postToBeUpdated));
    }

    public List<Post> updatePosts(List<Post> posts) {
        return postRepository.saveAll(posts);
    }

    public void deletePost(long postId) {
        Post postToBeDeleted = getPost(postId);

        if (postToBeDeleted.isDeleted()) {
            throw new DataValidationException(RE_DELETING_POST_EXCEPTION.getMessage());
        }

        postToBeDeleted.setDeleted(true);

        postMapper.toDto(postRepository.save(postToBeDeleted));
    }

    public PostDto getPostById(long postId) {
        Post post = getPost(postId);
        PostDto postDto = postMapper.toDto(post);
        log.info("Post has been received.");

        kafkaPostViewProducer.sendPostViewEvent(post);
        log.info("PostViewEvent has been sent from PostService.");

        return postDto;
    }


    public List<Post> getAllDrafts() {
        return postRepository.findAllDrafts();
    }

    public List<PostDto> getDraftsOfUser(long userId) {
        postVerifier.verifyUserExistence(userId);

        return getSortedDrafts(postRepository.findByAuthorId(userId));
    }

    public List<PostDto> getDraftsOfProject(long projectId) {
        postVerifier.verifyProjectExistence(projectId);

        return getSortedDrafts(postRepository.findByProjectId(projectId));
    }

    public List<PostDto> getPostsOfUser(long userId) {
        postVerifier.verifyUserExistence(userId);

        return getSortedPosts(postRepository.findByAuthorId(userId));
    }

    public List<PostDto> getPostsOfProject(long projectId) {
        postVerifier.verifyProjectExistence(projectId);

        return getSortedPosts(postRepository.findByProjectId(projectId));
    }

    private Post getPost(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new DataValidationException(NON_EXISTING_POST_EXCEPTION.getMessage()));
    }

    private List<PostDto> getSortedDrafts(List<Post> posts) {
        return posts.stream()
                .filter(post -> !post.isPublished())
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(postMapper::toDto)
                .toList();
    }

    private List<PostDto> getSortedPosts(List<Post> posts) {
        return posts.stream()
                .filter(Post::isPublished)
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(postMapper::toDto)
                .toList();
    }

    private void cachePost(PostRedisDto postRedisDto) {
        postRedisDto.setTtl(timeToLive);

        postCache.save(postRedisDto);
    }

    private void cacheAuthor(Long authorId) {
        UserDto userDto = userServiceClient.getUser(authorId);

        UserRedisDto userRedisDto = new UserRedisDto();
        userRedisDto.setUsername(userDto.getUsername());
        userRedisDto.setTtl(timeToLive);

        userCache.save(userRedisDto);
    }
}
