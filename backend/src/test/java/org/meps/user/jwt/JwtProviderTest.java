package org.meps.user.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT 토큰 파싱")
class JwtProviderTest {

    // HS256 키 최소 길이(32바이트) 이상
    private static final String SECRET = "test-secret-key-for-jwt-provider-unit-test-0123456789";

    private JwtProvider newProvider(String secret, long expirationMs) {
        JwtProvider provider = new JwtProvider();
        ReflectionTestUtils.setField(provider, "secret", secret);
        ReflectionTestUtils.setField(provider, "expirationMs", expirationMs);
        provider.init();
        return provider;
    }

    @Test
    @DisplayName("발급한 토큰에서 userId를 복원한다")
    void getUserId_validToken() {
        JwtProvider provider = newProvider(SECRET, 3600_000L);

        String token = provider.createToken(42);

        assertThat(provider.getUserId(token)).isEqualTo(42);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 null을 반환한다")
    void getUserId_wrongSignature() {
        JwtProvider provider = newProvider(SECRET, 3600_000L);
        JwtProvider other = newProvider("another-secret-key-for-signature-mismatch-test-987654", 3600_000L);

        String token = other.createToken(42);

        assertThat(provider.getUserId(token)).isNull();
    }

    @Test
    @DisplayName("만료된 토큰은 null을 반환한다")
    void getUserId_expiredToken() {
        JwtProvider provider = newProvider(SECRET, -1000L);

        String token = provider.createToken(42);

        assertThat(provider.getUserId(token)).isNull();
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 null을 반환한다")
    void getUserId_malformedToken() {
        JwtProvider provider = newProvider(SECRET, 3600_000L);

        assertThat(provider.getUserId("not-a-jwt")).isNull();
    }

    @Test
    @DisplayName("null 토큰은 null을 반환한다")
    void getUserId_nullToken() {
        JwtProvider provider = newProvider(SECRET, 3600_000L);

        assertThat(provider.getUserId(null)).isNull();
    }
}
