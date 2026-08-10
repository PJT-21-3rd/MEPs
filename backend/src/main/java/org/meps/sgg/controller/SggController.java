package org.meps.sgg.controller;

import lombok.RequiredArgsConstructor;
import org.meps.sgg.dto.SggBriefingResponseDto;
import org.meps.hjd.service.HjdService;
import org.meps.sgg.service.SggService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sgg")
@RequiredArgsConstructor
public class SggController {

    private final SggService sggService;

    /**
     * 구별 AI 브리핑 조회
     */
    @GetMapping("/{sggCd}/briefing")
    public SggBriefingResponseDto getSggBriefing(@PathVariable("sggCd") String sggCd) {
        return sggService.getSggBriefing(sggCd);
    }
}
