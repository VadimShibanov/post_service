package faang.school.postservice.dto.redis;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@RedisHash("User")
@Data
public class UserRedisDto implements Serializable {

    @Id
    private Long id;

    private String username;

    @TimeToLive
    private Integer ttl;
}
