package collectors.buildings;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 공공데이터포털(apis.data.go.kr) 전용 HTTP 클라이언트
 *
 * 사용처: BrTitleClient(표제부), BrFloorClient(층별개요)
 *        VWorld 계열 클라이언트는 common.ApiClient를 사용한다.
 *
 * common.ApiClient를 쓰지 않는 이유:
 *   data.go.kr 서버가 HTTP/2 요청에 200 + 빈 body를 반환하는 문제가 있어
 *   HTTP/1.1 고정과 User-Agent 헤더가 필요한데, ApiClient는 팀 공용 코드라
 *   직접 수정하지 않고 별도 클라이언트로 분리함.
 *   (ApiClient에 해당 수정이 반영되면 이 클래스는 제거하고 ApiClient로 통일 가능)
 *
 * 실패 처리: 3회까지 재시도(1초→2초 백오프) 후 예외.
 *          호출 측(BuildingCollector)이 잡아서 해당 필지만 건너뛴다.
 */

public class DataGoClient {

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)   // HTTP/2 빈 응답 이슈 회피 — 검증 완료
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 200) return res.body();
                System.err.printf("HTTP %d (attempt %d): %s%n", res.statusCode(), attempt, url);
            } catch (Exception e) {
                System.err.printf("Request failed (attempt %d): %s%n", attempt, e.getMessage());
            }
            sleep(1000L * attempt);
        }
        throw new RuntimeException("API call failed after 3 attempts: " + url);
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}