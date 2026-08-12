package org.meps.common.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.user.jwt.JwtProvider;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("@LoginUser 선택적 인증 리졸버")
class LoginUserArgumentResolverTest {

    private static final String SECRET = "test-secret-key-for-login-user-resolver-test-0123456789";

    private JwtProvider jwtProvider;
    private LoginUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secret", SECRET);
        ReflectionTestUtils.setField(jwtProvider, "expirationMs", 3600_000L);
        ReflectionTestUtils.invokeMethod(jwtProvider, "init");
        resolver = new LoginUserArgumentResolver(jwtProvider);
    }

    // supportsParameter 검증용 시그니처 샘플
    @SuppressWarnings("unused")
    static class SampleController {
        void handle(@LoginUser Integer userId, String plain) {
        }
    }

    @Test
    @DisplayName("@LoginUser가 붙은 파라미터만 지원한다")
    void supportsParameter_onlyAnnotated() throws Exception {
        Method method = SampleController.class.getDeclaredMethod("handle", Integer.class, String.class);

        assertThat(resolver.supportsParameter(new MethodParameter(method, 0))).isTrue();
        assertThat(resolver.supportsParameter(new MethodParameter(method, 1))).isFalse();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 userId를 주입한다")
    void resolveArgument_validToken() {
        NativeWebRequest request = requestWithAuthorization("Bearer " + jwtProvider.createToken(7));

        assertThat(resolver.resolveArgument(null, null, request, null)).isEqualTo(7);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 null을 주입한다")
    void resolveArgument_noHeader() {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        assertThat(resolver.resolveArgument(null, null, request, null)).isNull();
    }

    @Test
    @DisplayName("Bearer 접두사가 없으면 null을 주입한다")
    void resolveArgument_noBearerPrefix() {
        NativeWebRequest request = requestWithAuthorization(jwtProvider.createToken(7));

        assertThat(resolver.resolveArgument(null, null, request, null)).isNull();
    }

    @Test
    @DisplayName("무효 토큰이면 차단하지 않고 null을 주입한다")
    void resolveArgument_invalidToken() {
        NativeWebRequest request = requestWithAuthorization("Bearer not-a-jwt");

        assertThat(resolver.resolveArgument(null, null, request, null)).isNull();
    }

    private NativeWebRequest requestWithAuthorization(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", value);
        return new ServletWebRequest(request);
    }
}
