package org.meps.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.user.dto.UserDto;

@Mapper
public interface UserMapper {

    /** 이메일 중복 확인 */
    int countByEmail(@Param("email") String email);

    /** 회원 등록 — userId가 생성된 값으로 채워진다 */
    int insertUser(UserDto user);

    UserDto findByEmail(@Param("email") String email);

    /** 찜 존재 여부 확인 */
    boolean existsSavedBuilding(@Param("userId") Integer userId, @Param("buildingId") String buildingId);

    /** 찜 등록 */
    void insertSavedBuilding(@Param("userId") Integer userId, @Param("buildingId") String buildingId);

    /** 찜 해제 */
    void deleteSavedBuilding(@Param("userId") Integer userId, @Param("buildingId") String buildingId);

    /** 건물의 현재 찜 개수 조회 (테스트/검증용) */
    int findSavedCount(@Param("buildingId") String buildingId);

    /** 건물 찜 개수 증가 */
    void incrementSavedCount(@Param("buildingId") String buildingId);

    /** 건물 찜 개수 감소 */
    void decrementSavedCount(@Param("buildingId") String buildingId);
}