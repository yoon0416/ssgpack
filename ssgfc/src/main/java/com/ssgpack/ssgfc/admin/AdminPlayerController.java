package com.ssgpack.ssgfc.admin;

import com.ssgpack.ssgfc.player.Player;
import com.ssgpack.ssgfc.player.PlayerRepository;
import com.ssgpack.ssgfc.util.UtilUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin/player")
@RequiredArgsConstructor
public class AdminPlayerController {

    private final PlayerRepository playerRepository;
    private final UtilUpload utilUpload;  // ✅ UtilUpload 주입

    @GetMapping("/list")
    public String listPlayers(Model model) {
        List<Player> players = playerRepository.findAll();

        Map<String, List<Player>> positionMap = new LinkedHashMap<>();
        for (Player player : players) {
            String pos = (player.getPosition() != null) ? player.getPosition().trim() : "기타";
            positionMap.computeIfAbsent(pos, k -> new ArrayList<>()).add(player);
        }

        model.addAttribute("positionMap", positionMap);
        return "admin/player/list";
    }

    @GetMapping("/add")
    public String addPlayerForm(Model model) {
        Player player = new Player();
        player.setPno("");  // ✅ 기본값 세팅 (비워둬도 되지만 명시적으로 추가)
        model.addAttribute("player", player);
        return "admin/player/add";
    }


    @PostMapping("/add")
    public String addPlayer(@RequestParam("name") String name,
                             @RequestParam(value = "position", required = false) String position,
                             @RequestParam(value = "backNumber", required = false) String backNumber,
                             @RequestParam(value = "birthDate", required = false) String birthDate,
                             @RequestParam(value = "school", required = false) String school,
                             @RequestParam(value = "draftInfo", required = false) String draftInfo,
                             @RequestParam(value = "activeYears", required = false) String activeYears,
                             @RequestParam(value = "teams", required = false) String teams,
                             @RequestParam("playerImage") MultipartFile file) {

        Player player = new Player();
        player.setName(name);
        player.setPosition(position);
        player.setBackNumber(backNumber);
        player.setSchool(school);
        player.setDraftInfo(draftInfo);
        player.setActiveYears(activeYears);
        player.setTeams(teams);

        if (birthDate != null && !birthDate.isEmpty()) {
            try {
                player.setBirthDate(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
                e.printStackTrace();
                player.setBirthDate(null);
            }
        }

        try {
            String saveName = utilUpload.fileUpload(file, "players/");
            if (saveName != null) {
                player.setImageUrl("/images/players/" + saveName);
            } else {
                player.setImageUrl("/images/players/default.png");
            }
        } catch (IOException e) {
            e.printStackTrace();
            player.setImageUrl("/images/players/default.png");
        }

        playerRepository.save(player);
        return "redirect:/admin/player/list";
    }

    @GetMapping("/edit/{id}")
    public String editPlayerForm(@PathVariable Long id, Model model) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 선수입니다."));
        model.addAttribute("player", player);
        return "admin/player/edit";
    }

    @PostMapping("/edit/{id}")
    public String editPlayer(@PathVariable Long id,
                              @RequestParam("name") String name,
                              @RequestParam(value = "position", required = false) String position,
                              @RequestParam(value = "backNumber", required = false) String backNumber,
                              @RequestParam(value = "birthDate", required = false) String birthDate,
                              @RequestParam(value = "school", required = false) String school,
                              @RequestParam(value = "draftInfo", required = false) String draftInfo,
                              @RequestParam(value = "activeYears", required = false) String activeYears,
                              @RequestParam(value = "teams", required = false) String teams,
                              @RequestParam("playerImage") MultipartFile file) {

        Player existing = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 선수입니다."));

        existing.setName(name);
        existing.setPosition(position);
        existing.setBackNumber(backNumber);
        existing.setSchool(school);
        existing.setDraftInfo(draftInfo);
        existing.setActiveYears(activeYears);
        existing.setTeams(teams);

        if (birthDate != null && !birthDate.isEmpty()) {
            try {
                existing.setBirthDate(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
                e.printStackTrace();
                existing.setBirthDate(null);
            }
        }

        if (file != null && !file.isEmpty()) {
            try {
                String saveName = utilUpload.fileUpload(file, "players/");
                if (saveName != null) {
                    existing.setImageUrl("/images/players/" + saveName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playerRepository.save(existing);
        return "redirect:/admin/player/view/" + id;
    }

    @GetMapping("/view/{id}")
    public String viewPlayer(@PathVariable Long id, Model model) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 선수입니다."));
        model.addAttribute("player", player);
        return "admin/player/view";
    }

    @PostMapping("/delete/{id}")
    public String deletePlayer(@PathVariable Long id) {
        playerRepository.deleteById(id);
        return "redirect:/admin/player/list";
    }
}
