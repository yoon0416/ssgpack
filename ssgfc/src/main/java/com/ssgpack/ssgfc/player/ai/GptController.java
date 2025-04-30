package com.ssgpack.ssgfc.player.ai;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class GptController {

    private final GptAnalysisService gptService;

    public GptController(GptAnalysisService gptService) {
        this.gptService = gptService;
    }

    @PostMapping("/analyze")
    public String analyze(@RequestBody String playerJson) throws Exception {
        return gptService.analyze(playerJson);
    }
}
