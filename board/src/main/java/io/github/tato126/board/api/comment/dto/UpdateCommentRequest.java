package io.github.tato126.board.api.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateCommentRequest {

    @NotBlank(message = "내용은 필수입니다")
    private String content;
}
