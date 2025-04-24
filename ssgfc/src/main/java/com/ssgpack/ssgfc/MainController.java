package com.ssgpack.ssgfc;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardService;
import com.ssgpack.ssgfc.vote.VoteService;
import com.ssgpack.ssgfc.vote.VoteTitle;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    private final BoardService boardService;
    private final VoteService voteService;

    public MainController(BoardService boardService, VoteService voteService) {
        this.boardService = boardService;
        this.voteService = voteService;
    }

    // ✅ 메인 페이지에서 인기글과 최신 투표를 모델에 담아 전달합니다.
    @GetMapping("/")
    public String home(Model model) {
        List<Board> popularBoards = boardService.getPopularBoards(3);
        model.addAttribute("popularBoards", popularBoards);

        // ✅ 최신 투표 1개와 해당 선택지들을 모델에 추가
        VoteTitle latestVote = voteService.getLatestVote();
        if (latestVote != null) {
            model.addAttribute("latestVote", latestVote);
            model.addAttribute("latestVoteOptions", voteService.getContentsByTitleId(latestVote.getId()));
        }

        return "main";
    }
}