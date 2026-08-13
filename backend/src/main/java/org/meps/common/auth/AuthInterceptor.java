package org.meps.common.auth;

import lombok.RequiredArgsConstructor;
import org.meps.user.jwt.JwtProvider;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 인증 필수 API의 토큰 검증 — 부재·무효 시 401.
 * userId 주입은 @LoginUser(LoginUserArgumentResolver)가 담당한다.
 */
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS preflight는 Authorization 헤더 없이 오므로 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new LoginRequiredException("Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
        }

        // getUserId는 무효 토큰에 대해 예외 대신 null을 반환한다
        if (jwtProvider.getUserId(header.substring(BEARER_PREFIX.length())) == null) {
            throw new LoginRequiredException("유효하지 않은 토큰입니다.");
        }
        return true;
    }
}