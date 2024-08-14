package faang.school.postservice.controller.feed;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.redis.PostRedisDto;
import faang.school.postservice.service.feed.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    private final UserContext userContext;

    @GetMapping("/feed")
    public List<PostRedisDto> feed(@RequestParam(required = false) Long lastViewedPostId) {
        return feedService.getFeed(userContext.getUserId(), lastViewedPostId);
    }

    @GetMapping("/heat")
    public void heat() {
        feedService.heat();
    }
}
