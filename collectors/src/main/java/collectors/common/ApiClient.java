package collectors.common;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** GET 호출, 실패 시 최대 3회 재시도 */
    public static String get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
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
            sleep(1000L * attempt);  // 1s, 2s 백오프
        }
        throw new RuntimeException("API call failed after 3 attempts: " + url);
    }

    public static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}