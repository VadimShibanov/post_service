package faang.school.postservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CommentEventDto extends EventDto {

    private Long commentAuthorId;

    private Long postId;

    private String content;

    private LocalDateTime createdAt;
}
