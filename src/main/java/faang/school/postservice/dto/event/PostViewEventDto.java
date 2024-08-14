package faang.school.postservice.dto.event;

import lombok.Data;

@Data
public class PostViewEventDto extends EventDto {

    private Long postId;
}
