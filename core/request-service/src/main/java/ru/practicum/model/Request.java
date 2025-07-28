package ru.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.stats.RequestStatus;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    long id;

    @Column(name = "requester_id")
    long requesterId;

    @Column(name = "event_id")
    long eventId;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Enumerated(EnumType.STRING)
    RequestStatus status;
}
