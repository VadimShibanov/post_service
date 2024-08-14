package faang.school.postservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FeedHeatEventDto extends EventDto {

    private List<Long> usersIds;
}
