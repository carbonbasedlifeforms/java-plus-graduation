package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.UserClient;
import ru.practicum.dal.CommentRepository;
import ru.practicum.dal.EventRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.service.CommentService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto save(Long userId, Long eventId, NewCommentDto newCommentDto) {
        Event event = checkExistsAndReturnEvent(eventId);
        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Event is not published");
        }
        Comment comment = commentMapper.getComment(
                newCommentDto,
                event,
                userId
        );
        return commentMapper.getCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getAll(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.ASC, "id"));
        return commentRepository.findAllByEventId(pageable, eventId).stream()
                .map(commentMapper::getCommentDto)
                .toList();
    }

    @Transactional
    @Override
    public CommentDto update(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto) {
        Comment comment = validateCommentForEvent(eventId, commentId, userId);
        comment.setMessage(newCommentDto.getMessage());
        return commentMapper.getCommentDto(comment);
    }

    @Override
    public void delete(Long userId, Long eventId, Long commentId) {
        Comment comment = validateCommentForEvent(eventId, commentId, userId);
        commentRepository.delete(comment);
    }

    @Override
    public CommentDto adminUpdate(Long eventId, Long commentId, NewCommentDto newCommentDto) {
        Comment comment = validateCommentForEvent(eventId, commentId);
        comment.setMessage(newCommentDto.getMessage());
        return commentMapper.getCommentDto(comment);
    }

    @Override
    public void adminDelete(Long eventId, Long commentId) {
        Comment comment = validateCommentForEvent(eventId, commentId);
        commentRepository.delete(comment);
    }

    private UserShortDto checkExistsAndReturnUser(Long userId) {
        return userClient.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %d not found".formatted(userId)));
    }

    private Event checkExistsAndReturnEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id %d not found".formatted(eventId)));
    }

    private Comment checkExistsAndReturnComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id %d not found".formatted(commentId)));
    }

    private Comment validateCommentForEvent(Long eventId, Long commentId, Long userId) {
        Event event = checkExistsAndReturnEvent(eventId);
        Comment comment = checkExistsAndReturnComment(commentId);

        if (comment.getEvent().getId() != event.getId()) {
            throw new ConflictException("Comment with id %d is not belong to event with id %d"
                    .formatted(comment.getId(), event.getId()));
        }
        if (userId.equals(comment.getAuthorId())) {
            throw new ConflictException("Comment with id %d is not created by user with id %d"
                    .formatted(comment.getAuthorId(), userId));
        }
        return comment;
    }

    private Comment validateCommentForEvent(Long eventId, Long commentId) {
        Event event = checkExistsAndReturnEvent(eventId);
        Comment comment = checkExistsAndReturnComment(commentId);

        if (comment.getEvent().getId() != event.getId()) {
            throw new ConflictException("Comment with id %d is not belong to event with id %d"
                    .formatted(comment.getId(), event.getId()));
        }
        return comment;
    }
}
