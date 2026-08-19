package org.meps.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.user.dto.RefreshTokenDto;

@Mapper
public interface RefreshTokenMapper {

    /** 발급/갱신 — 회원당 1행이므로 있으면 덮어쓴다 */
    void upsert(RefreshTokenDto refreshToken);

    /** 토큰으로 조회 (재발급 검증용) */
    RefreshTokenDto findByToken(@Param("token") String token);

    /** 토큰 삭제 (로그아웃) */
    void deleteByToken(@Param("token") String token);
}