package faang.school.postservice.dto.redis;

import faang.school.postservice.dto.comment.CommentDto;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@RedisHash("Post")
@Data
@Builder
public class PostRedisDto implements Serializable {

    @Id
    private Long id;

    private String content;

    private Long authorId;

    private UserRedisDto author;

    private LocalDateTime publishedAt;

    private long views;

    private long likes;

    private Set<CommentDto> comments;

    @TimeToLive
    private Integer ttl;
}
