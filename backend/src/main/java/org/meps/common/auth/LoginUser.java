package org.meps.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터로 로그인 사용자 ID(Integer)를 주입받는다.
 * 토큰이 없거나 유효하지 않으면 null — 비로그인 허용 API에서 응답 분기용
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}
