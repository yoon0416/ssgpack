package com.ssgpack.ssgfc.admin;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ssgpack.ssgfc.game.GameSchedule;
import com.ssgpack.ssgfc.game.GameScheduleRepository;

import lombok.RequiredArgsConstructor;



@Controller
@RequestMapping("/admin/schedule")
@RequiredArgsConstructor
public class AdminGameScheduleController {

    private final GameScheduleRepository gameScheduleRepository;

    // 목록
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("scheduleList", gameScheduleRepository.findAllByOrderByGameDateAsc());
        return "admin/schedule/list";
    }

    // 등록 폼
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("gameSchedule", new GameSchedule());
        return "admin/schedule/form";
    }

    // 등록 처리
    @PostMapping("/create")
    public String createSubmit(@ModelAttribute GameSchedule gameSchedule) {
        if (gameSchedule.getGameDate() == null) {
            return "redirect:/admin/schedule/list";
        }
        gameScheduleRepository.save(gameSchedule);
        return "redirect:/admin/schedule/list";
    }

    // 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<GameSchedule> gameSchedule = gameScheduleRepository.findById(id);
        if (gameSchedule.isPresent()) {
            model.addAttribute("gameSchedule", gameSchedule.get());
            return "admin/schedule/form";
        } else {
            return "redirect:/admin/schedule/list";
        }
    }

    // 수정 처리
    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable Long id, @ModelAttribute GameSchedule updatedSchedule) {
        Optional<GameSchedule> optionalExisting = gameScheduleRepository.findById(id);

        if (optionalExisting.isPresent()) {
            GameSchedule existing = optionalExisting.get();

            existing.setGameDate(updatedSchedule.getGameDate());
            existing.setLocation(updatedSchedule.getLocation());
            existing.setResult(updatedSchedule.getResult());
            existing.setReport(updatedSchedule.getReport());
            existing.setTeam1(updatedSchedule.getTeam1());
            existing.setTeam2(updatedSchedule.getTeam2());
            existing.setScore1(updatedSchedule.getScore1());
            existing.setScore2(updatedSchedule.getScore2());
            existing.setStartTime(updatedSchedule.getStartTime());

            gameScheduleRepository.save(existing);
        }

        return "redirect:/admin/schedule/list";
    }


    // 삭제
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        gameScheduleRepository.deleteById(id);
        return "redirect:/admin/schedule/list";
    }

    // 리플렉션으로 필드 수정하는 유틸 메서드
    private void setField(GameSchedule target, String fieldName, Object value) throws Exception {
        Field field = GameSchedule.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    //	컨트롤러 들어오기 전에 LocalDate로 변환됨
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });
    }

}
