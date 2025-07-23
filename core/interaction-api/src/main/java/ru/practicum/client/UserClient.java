package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.circuit_breaker.UserClientFallback;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@FeignClient(name = "user-service", path = "/admin/users", fallback = UserClientFallback.class)
public interface UserClient {

    @PostMapping
    UserDto newUser(@RequestBody NewUserDto dto);

    @GetMapping
    Collection<UserDto> findAllUsers(@RequestParam(required = false) List<Long> ids,
                                     @RequestParam(name = "from", defaultValue = "0") int from,
                                     @RequestParam(name = "size", defaultValue = "10") int size);

    @DeleteMapping("/{userId}")
    void deleteUser(@PathVariable Long userId);

    @GetMapping("/{userId}")
    Optional<UserShortDto> findUserById(@PathVariable Long userId);
}
