package ru.practicum.dto.event.enums;

import lombok.Getter;

@Getter
public enum FallBackUtil {
    FALLBACK_MESSAGE("service is temporarily unavailable");

    private final String message;

    FallBackUtil(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
