package ru.practicum.circuit_breaker;

import org.springframework.stereotype.Component;
import ru.practicum.client.UserClient;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.event.enums.FallBackUtil;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exceptions.ServiceFallBackException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDto newUser(NewUserDto dto) {
        throw new ServiceFallBackException(FallBackUtil.FALLBACK_MESSAGE.getMessage());
    }

    @Override
    public Collection<UserDto> findAllUsers(List<Long> ids, int from, int size) {
        throw new ServiceFallBackException(FallBackUtil.FALLBACK_MESSAGE.getMessage());
    }

    @Override
    public void deleteUser(Long userId) {
        throw new ServiceFallBackException(FallBackUtil.FALLBACK_MESSAGE.getMessage());
    }

    @Override
    public Optional<UserShortDto> findUserById(Long userId) {
        throw new ServiceFallBackException(FallBackUtil.FALLBACK_MESSAGE.getMessage());
    }
}
