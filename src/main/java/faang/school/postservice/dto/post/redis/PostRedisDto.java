package faang.school.postservice.dto.post.redis;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@RedisHash("Post")
@Data
@NoArgsConstructor
public class PostRedisDto implements Serializable {

    @Id
    private long id;

    private String content;

    private Long authorId;

    @TimeToLive
    private Integer ttl;
}
