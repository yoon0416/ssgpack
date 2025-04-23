package com.ssgpack.ssgfc.vote;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRole;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vote")
public class VoteController {

    private final VoteService voteService;
    private final VoteTitleRepository voteTitleRepository;
    private final VoteContentRepository voteContentRepository;

    //  투표 목록
    @GetMapping("")
    public String voteList(Model model) {
        model.addAttribute("votes", voteService.getAllVotes());
        return "vote/list";
    }

    //  투표 상세 보기 + 실시간 결과
    @GetMapping("/{id}")
    public String voteDetail(@PathVariable Long id, Model model) {
        VoteTitle voteTitle = voteTitleRepository.findById(id).orElseThrow();
        List<VoteContent> contents = voteService.getContentsByTitleId(id);

        // 전체 투표 수 계산
        long totalVotes = contents.stream()
                .mapToLong(content -> voteService.getVoteCount(content))
                .sum();

        model.addAttribute("voteTitleId", id);
        model.addAttribute("contents", contents);
        model.addAttribute("totalVotes", totalVotes);

        return "vote/detail";
    }

    //  투표 제출
    @PostMapping("/submit")
    public String submitVote(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam Long voteTitleId,
                             @RequestParam(required = false) Long voteContentId,
                             Model model) {
        try {
            User user = userDetails.getUser();
            voteService.submitVote(user, voteTitleId, voteContentId);
            return "redirect:/vote";
        } catch (Exception e) {
            VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId).orElseThrow();
            List<VoteContent> contents = voteService.getContentsByTitleId(voteTitleId);
            long totalVotes = contents.stream()
                    .mapToLong(content -> voteService.getVoteCount(content))
                    .sum();

            model.addAttribute("error", e.getMessage());
            model.addAttribute("voteTitleId", voteTitleId);
            model.addAttribute("contents", contents);
            model.addAttribute("totalVotes", totalVotes);
            return "vote/detail";
        }
    }

    //  투표 생성 폼 (관리자만)
    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        checkVoteAuthority(userDetails.getUser());

        VoteForm form = new VoteForm();
        form.getContents().add("");
        form.getContents().add("");
        model.addAttribute("form", form);
        return "vote/create";
    }

    //  투표 생성 처리 (관리자만)
    @PostMapping("/create")
    public String createSubmit(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @ModelAttribute VoteForm form) {
        checkVoteAuthority(userDetails.getUser());

        VoteTitle voteTitle = VoteTitle.builder()
                .title(form.getTitle())
                .createDate(LocalDateTime.now())
                .build();

        voteTitle = voteTitleRepository.save(voteTitle);

        for (String content : form.getContents()) {
            if (!content.isBlank()) {
                VoteContent vc = VoteContent.builder()
                        .content(content)
                        .voteTitle(voteTitle)
                        .build();
                voteContentRepository.save(vc);
            }
        }

        return "redirect:/vote";
    }

    //  권한 체크 (마스터 or 게시판 관리자만)
    private void checkVoteAuthority(User user) {
        if (user.getRole() != UserRole.MASTER.getCode() &&
            user.getRole() != UserRole.BOARD_MANAGER.getCode()) {
            throw new AccessDeniedException("투표 생성 권한이 없습니다.");
        }
    }
    
 // 투표 삭제 (관리자만 가능)
    @PostMapping("/delete/{id}")
    public String deleteVote(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable Long id) {
        checkVoteAuthority(userDetails.getUser());

        voteService.deleteVote(id);

        return "redirect:/vote";
    }

}
