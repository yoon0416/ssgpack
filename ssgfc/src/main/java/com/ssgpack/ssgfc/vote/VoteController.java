package com.ssgpack.ssgfc.vote;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // ✅ 전체 투표 목록 조회
    @GetMapping("")
    public String voteList(Model model) {
        model.addAttribute("votes", voteService.getAllVotes());
        return "vote/list";
    }

    // ✅ 투표 상세 페이지 출력 (선택지 + 실시간 결과 포함)
    @GetMapping("/{id}")
    public String voteDetail(@PathVariable Long id, Model model) {
        VoteTitle voteTitle = voteTitleRepository.findById(id).orElseThrow();
        List<VoteContent> contents = voteService.getContentsByTitleId(id);

        long totalVotes = contents.stream()
                .mapToLong(content -> voteService.getVoteCount(content))
                .sum();

        model.addAttribute("voteTitleId", id);
        model.addAttribute("contents", contents);
        model.addAttribute("totalVotes", totalVotes);

        return "vote/detail";
    }

    // ✅ 투표 제출 처리 (중복 확인 포함)
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

    // ✅ 투표 생성 폼 출력 (관리자만 가능)
    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        checkVoteAuthority(userDetails.getUser());

        VoteForm form = new VoteForm();
        form.getContents().add("");
        form.getContents().add("");
        model.addAttribute("form", form);
        return "vote/create";
    }

    // ✅ 투표 생성 처리 (이미지 포함, 관리자만 가능)
    @PostMapping("/create")
    public String createSubmit(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @ModelAttribute VoteForm form,
                               @RequestParam("img") MultipartFile file) {
        checkVoteAuthority(userDetails.getUser());

        // ✅ 이미지 파일까지 포함한 투표 저장 처리
        voteService.insertVote(form, file);

        return "redirect:/vote";
    }

    // ✅ 투표 삭제 처리 (관리자만 가능)
    @PostMapping("/delete/{id}")
    public String deleteVote(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable Long id) {
        checkVoteAuthority(userDetails.getUser());
        voteService.deleteVote(id);
        return "redirect:/vote";
    }

    // ✅ 관리자 권한 확인 (MASTER 또는 BOARD_MANAGER만 허용)
    private void checkVoteAuthority(User user) {
        if (user.getRole() != UserRole.MASTER.getCode() &&
            user.getRole() != UserRole.BOARD_MANAGER.getCode()) {
            throw new AccessDeniedException("투표 생성 권한이 없습니다.");
        }
    }
}
