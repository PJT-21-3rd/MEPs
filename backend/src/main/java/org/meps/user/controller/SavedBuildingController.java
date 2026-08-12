package org.meps.user.controller;

import lombok.RequiredArgsConstructor;
import org.meps.user.exception.InvalidTokenException;
import org.meps.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class SavedBuildingController {

    private final UserService userService;

    /** 찜하기 등록 */
    @PostMapping("/saved/{buildingId}")
    public ResponseEntity<Void> saveBuilding(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("buildingId") String buildingId) {

        userService.saveBuilding(extractToken(authHeader), buildingId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 찜하기 해제 */
    @DeleteMapping("/saved/{buildingId}")
    public ResponseEntity<Void> unsaveBuilding(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("buildingId") String buildingId) {

        userService.unsaveBuilding(extractToken(authHeader), buildingId);
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        return authHeader.substring(7).trim();
    }
}