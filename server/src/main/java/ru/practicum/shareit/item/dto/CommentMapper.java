package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
                );
    }

    public Comment toComment(CommentDto commentDto) {
        return new Comment(commentDto.getId(),
               commentDto.getText(),
                null,
                null,
                LocalDateTime.now()
                );
    }
}
