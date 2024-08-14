package faang.school.postservice.client;

import faang.school.postservice.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "${user-service.host}:${user-service.port}")
public interface UserServiceClient {

    @GetMapping("/users/{userId}")
    UserDto getUser(@PathVariable long userId);

    @PostMapping("/users")
    List<UserDto> getUsersByIds(@RequestBody List<Long> ids);

    @GetMapping("/users/{userId}/exists")
    boolean existsById(@PathVariable long userId);

    @GetMapping("/subscriptions/followers/{followeeId}")
    List<Long> getFollowersIds(@PathVariable long followeeId);

    @GetMapping("/users/allUsersIds")
    List<Long> getAllUsersIds();

    @GetMapping("/subscriptions/followings/{followerId}")
    List<UserDto> getFollowings(@PathVariable long followerId);
}
