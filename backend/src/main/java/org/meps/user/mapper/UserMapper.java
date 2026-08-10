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


}