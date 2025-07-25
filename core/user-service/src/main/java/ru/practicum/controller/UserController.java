package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto newUser(@RequestBody @Valid NewUserDto dto) {

        log.info("Creating new user with body {}", dto);
        return userService.newUser(dto);
    }

    @GetMapping
    public Collection<UserDto> findAllUsers(@RequestParam(required = false) List<Long> ids,
                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                            @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Tryind to get all users from {}, size {}", from, size);
        return userService.getAllUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Trying to delete user with id {}", userId);
        userService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public Optional<UserShortDto> findUserById(@PathVariable Long userId) {
        log.info("Trying to get user with id {}", userId);
        return userService.findUserById(userId);
    }
}
