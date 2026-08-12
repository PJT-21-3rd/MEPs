package org.meps.user.controller;

import lombok.RequiredArgsConstructor;
import org.meps.common.auth.LoginUser;
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
            @LoginUser Integer userId,
            @PathVariable("buildingId") String buildingId) {

        userService.saveBuilding(userId, buildingId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 찜하기 해제 */
    @DeleteMapping("/saved/{buildingId}")
    public ResponseEntity<Void> unsaveBuilding(
            @LoginUser Integer userId,
            @PathVariable("buildingId") String buildingId) {

        userService.unsaveBuilding(userId, buildingId);
        return ResponseEntity.noContent().build();
    }
}