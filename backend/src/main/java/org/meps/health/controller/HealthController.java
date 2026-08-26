package org.meps.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ALB 헬스체크 대상 엔드포인트.
 * DB 등 외부 의존성을 확인하지 않고 프로세스 생존 여부만 응답한다.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
