package com.ssgpack.ssgfc.vote;

import java.util.List;
import java.util.stream.Collectors;

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
    public String voteDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        VoteTitle voteTitle = voteTitleRepository.findById(id).orElseThrow();
        List<VoteContent> contents = voteService.getContentsByTitleId(id);

        // ✅ 선택지 + 득표수 계산해서 DTO로 변환
        List<VoteContentDto> contentDtos = contents.stream()
                .map(content -> VoteContentDto.builder()
                        .id(content.getId())
                        .content(content.getContent())
                        .voteCount(voteService.getVoteCount(content))
                        .build())
                .collect(Collectors.toList());

        Long selectedContentId = null;
        if (userDetails != null) {
            selectedContentId = voteService.getUserSelectedContentId(userDetails.getUser().getId(), id);
        }

        model.addAttribute("voteTitle", voteTitle);
        model.addAttribute("contents", contentDtos);
        model.addAttribute("selectedContentId", selectedContentId);

        return "vote/detail";
    }

    // ✅ 투표 제출 처리 (중복 체크, 마감 체크 포함)
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
            // ✅ 실패 시 detail.html로 돌아가면서 에러 메시지 전달
            VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId).orElseThrow();
            List<VoteContent> contents = voteService.getContentsByTitleId(voteTitleId);

            List<VoteContentDto> contentDtos = contents.stream()
                    .map(content -> VoteContentDto.builder()
                            .id(content.getId())
                            .content(content.getContent())
                            .voteCount(voteService.getVoteCount(content))
                            .build())
                    .collect(Collectors.toList());

            model.addAttribute("error", e.getMessage()); // ✅ 에러 메시지 추가
            model.addAttribute("voteTitle", voteTitle);
            model.addAttribute("contents", contentDtos);
            model.addAttribute("selectedContentId", null); // 실패한 경우 선택지 체크 없음

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

    // ✅ 투표 생성 처리
    @PostMapping("/create")
    public String createSubmit(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @ModelAttribute VoteForm form,
                               @RequestParam("img") MultipartFile file) {
        checkVoteAuthority(userDetails.getUser());
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
    
    // ✅ 투표 수정 폼 출력 (관리자만 가능)
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        checkVoteAuthority(userDetails.getUser());

        VoteTitle voteTitle = voteTitleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 투표가 존재하지 않습니다."));

        VoteForm form = voteService.convertToForm(voteTitle);

        model.addAttribute("form", form);
        model.addAttribute("voteId", id);

        return "vote/edit"; // ✅ templates/vote/edit.html로 이동
    }
    
    // ✅ 투표 수정 처리 (POST - 관리자만 가능)
    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable Long id,
                             @ModelAttribute VoteForm form,
                             @RequestParam(value = "img", required = false) MultipartFile file,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        checkVoteAuthority(userDetails.getUser());

        try {
            // ✅ 수정 처리
            voteService.updateVote(id, form, file);  
            return "redirect:/vote";  // 수정 후 투표 목록으로 리다이렉트
        } catch (Exception e) {
            // ✅ 수정 실패 시 에러 메시지와 함께 수정 폼으로 돌아가면서 안내
            VoteTitle voteTitle = voteTitleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 투표가 존재하지 않습니다."));

            model.addAttribute("form", form);
            model.addAttribute("voteId", id);
            model.addAttribute("error", e.getMessage());  // 에러 메시지 전달

            return "vote/edit";  // 수정 폼으로 돌아가기
        }
    }
    
}
