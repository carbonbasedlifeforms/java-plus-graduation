package ru.practicum.dto.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class StatsHitDto {
    long id;

    @Size(max = 100)
    String app;

    @Size(max = 100)
    String uri;

    @Size(max = 100)
    String ip;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime timestamp;
}
