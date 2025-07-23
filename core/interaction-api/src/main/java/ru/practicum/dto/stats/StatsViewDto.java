package ru.practicum.dto.stats;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class StatsViewDto {
    String app;
    String uri;
    Long hits;
}
