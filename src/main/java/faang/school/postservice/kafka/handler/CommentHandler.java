package faang.school.postservice.kafka.handler;

import faang.school.postservice.dto.comment.CommentDto;
import faang.school.postservice.dto.event.CommentEventDto;
import faang.school.postservice.dto.redis.PostRedisDto;
import faang.school.postservice.repository.redis.RedisPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentHandler {

    private final RedisPostRepository postCache;

    private final ReentrantLock lock = new ReentrantLock();

    @Value("${spring.data.redis.comments-amount}")
    private int maxComments;

    public void addCommentToPost(CommentEventDto commentEventDto) {
        lock.lock();
        try {
            log.info("CommentHandler has been received CommentEventDto.");
            PostRedisDto postRedisDto = postCache.findById(commentEventDto.getPostId()).get();

            Set<CommentDto> comments = postRedisDto.getComments();

            if (comments == null) {
                comments = new LinkedHashSet<>();
            }

            if (comments.size() == maxComments) {
                CommentDto commentToRemove = comments.iterator().next();
                comments.remove(commentToRemove);
            }

            CommentDto commentDto = new CommentDto();
            commentDto.setContent(commentEventDto.getContent());
            commentDto.setAuthorId(commentEventDto.getCommentAuthorId());
            commentDto.setCreatedAt(commentEventDto.getCreatedAt());

            comments.add(commentDto);
            postRedisDto.setComments(comments);

            postCache.save(postRedisDto);
            log.info("PostRedisDto saved with comment from CommentEventDto.");
        } finally {
            lock.unlock();
        }
    }
}
