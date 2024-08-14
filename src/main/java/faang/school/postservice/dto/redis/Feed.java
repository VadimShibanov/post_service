package faang.school.postservice.dto.redis;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;


@RedisHash(value = "Feed")
@Builder
@Getter
public class Feed implements Serializable {

    @Id
    private Long userId;

    private List<Long> postsIds;
}
