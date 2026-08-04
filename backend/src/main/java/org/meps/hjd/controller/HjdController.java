package org.meps.hjd.controller;

import lombok.RequiredArgsConstructor;
import org.meps.hjd.dto.HjdNamesDto;
import org.meps.hjd.service.HjdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hjd")
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
}
