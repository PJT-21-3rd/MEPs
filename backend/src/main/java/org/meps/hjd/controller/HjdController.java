package org.meps.hjd.controller;

import lombok.RequiredArgsConstructor;
import org.meps.hjd.dto.HjdBriefingResponseDto;
import org.meps.hjd.dto.HjdNamesDto;
import org.meps.hjd.dto.SggBriefingResponseDto;
import org.meps.hjd.service.HjdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HjdController {

    private final HjdService hjdService;

    /**
     * 행정동 리스트 조회
     */
    @GetMapping
    public HjdNamesDto getNames() {
        return hjdService.getNames();
    }

    /**
     * 행정동별 AI 브리핑 조회
     */
    @GetMapping("/hjd/{hjdCd}/briefing")
    public HjdBriefingResponseDto getBriefing(@PathVariable("hjdCd") String hjdCd) {
        return hjdService.getBriefing(hjdCd);
    }

    /**
     * 구별 AI 브리핑 조회
     */
    @GetMapping("/ssg/{sggCd}/briefing")
    public SggBriefingResponseDto getSggBriefing(@PathVariable("sggCd") String sggCd) {
        return hjdService.getSggBriefing(sggCd);
    }
}
