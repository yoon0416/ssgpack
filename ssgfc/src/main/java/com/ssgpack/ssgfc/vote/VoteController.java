package com.ssgpack.ssgfc.vote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vote")
public class VoteController {

    private final VoteService voteService;
    private final VoteTitleRepository voteTitleRepository;
    private final VoteContentRepository voteContentRepository;

    @GetMapping("")
    public String voteList(Model model) {
        model.addAttribute("votes", voteService.getAllVotes());
        return "vote/list";
    }

    @GetMapping("/{id}")
    public String voteDetail(@PathVariable Long id, Model model) {
        VoteTitle voteTitle = voteTitleRepository.findById(id).orElseThrow();
        model.addAttribute("voteTitleId", id);
        List<VoteContent> contents = voteService.getContentsByTitleId(id);
        model.addAttribute("contents", contents);

        // 👇 결과 데이터 추가
        Map<String, Long> result = voteService.getVoteResult(voteTitle);
        model.addAttribute("result", result);

        return "vote/detail";
    }

    @PostMapping("/submit")
    public String submitVote(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam Long voteTitleId,
                             @RequestParam(required = false) Long voteContentId,
                             Model model) {

        try {
            User user = userDetails.getUser(); // ✅ 핵심 수정 포인트
            voteService.submitVote(user, voteTitleId, voteContentId);
            return "redirect:/vote";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("voteTitleId", voteTitleId);
            model.addAttribute("contents", voteService.getContentsByTitleId(voteTitleId));
            return "vote/detail";
        }
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        VoteForm form = new VoteForm();
        form.getContents().add("");
        form.getContents().add("");
        model.addAttribute("form", form);
        return "vote/create";
    }

    @PostMapping("/create")
    public String createSubmit(@ModelAttribute VoteForm form) {
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
}
