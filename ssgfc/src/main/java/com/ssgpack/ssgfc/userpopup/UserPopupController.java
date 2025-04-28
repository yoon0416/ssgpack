package com.ssgpack.ssgfc.userpopup;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.like.Like;
import com.ssgpack.ssgfc.board.like.LikeRepository;
import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/userpopup")
public class UserPopupController {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserPopupDto> getUserPopup(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 작성한 게시글
        List<BoardSimpleDto> boards = user.getBoard().stream()
                .map(board -> BoardSimpleDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .build())
                .collect(Collectors.toList());

        // 작성한 댓글
        List<CommentSimpleDto> comments = user.getComments().stream()
                .map(comment -> CommentSimpleDto.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .build())
                .collect(Collectors.toList());

        // 좋아요한 글
        List<LikeSimpleDto> likes = user.getLikes().stream()
                .map(like -> LikeSimpleDto.builder()
                        .boardId(like.getBoard().getId())
                        .title(like.getBoard().getTitle())
                        .build())
                .collect(Collectors.toList());

        // 최종 DTO
        UserPopupDto popupDto = UserPopupDto.builder()
                .nickName(user.getNick_name())
                .profileImg(user.getProfile_img())
                .introduce(user.getIntroduce())
                .createDate(user.getCreateDate())
                .boards(boards)
                .comments(comments)
                .likes(likes)
                .build();

        return ResponseEntity.ok(popupDto);
    }
}
