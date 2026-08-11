package org.meps.loan.controller;

import lombok.RequiredArgsConstructor;
import org.meps.loan.dto.LoanProductResponseDto;
import org.meps.loan.service.LoanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public LoanProductResponseDto getLoanProducts() {
        return loanService.getLoanProducts();
    }
}
