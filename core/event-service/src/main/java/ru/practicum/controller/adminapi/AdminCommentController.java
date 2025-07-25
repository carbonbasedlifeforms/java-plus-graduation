package ru.practicum.controller.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @PatchMapping("/{eventId}/comments/{comId}")
    public CommentDto updateComment(@PathVariable long eventId,
                                    @PathVariable long comId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Admin update comment with id {} for eventId {}", comId, eventId);
        return commentService.adminUpdate(eventId, comId, newCommentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{eventId}/comments/{comId}")
    public void deleteComment(@PathVariable long eventId,
                              @PathVariable long comId) {
        log.info("Admin delete comment with id {} for eventId {}", comId, eventId);
        commentService.adminDelete(eventId, comId);
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentDto> getComments(@PathVariable long eventId,
                                        @RequestParam(name = "from", defaultValue = "0") int from,
                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Admin get comments for eventId {}", eventId);
        return commentService.getAll(eventId, from, size);
    }
}
