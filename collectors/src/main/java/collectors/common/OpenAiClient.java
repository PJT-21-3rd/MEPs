package collectors.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class OpenAiClient {

    private static final String API_URL = Config.get("ai.apiUrl");
    private static final String DEFAULT_MODEL = Config.get("ai.defaultModel");
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RETRIES = 3;

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiClient() {
        this(System.getenv("OPENAI_API_KEY"), DEFAULT_MODEL);
    }

    public OpenAiClient(String apiKey, String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 설정되지 않았습니다.");
        }
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public record BriefingResult(String text, String modelName) {
    }

    /**
     * @param systemPrompt 역할/톤/제약을 지정하는 시스템 프롬프트
     * @param userPrompt   실제 통계 데이터를 담은 사용자 프롬프트
     */
    public BriefingResult generateBriefing(String systemPrompt, String userPrompt) throws IOException, InterruptedException {

        HttpRequest request = createRequest(systemPrompt, userPrompt);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseResponse(response);
            }

            boolean retryable = response.statusCode() == 429 || response.statusCode() >= 500;

            if (!retryable) {
                throw new IOException(
                        "OpenAI API error: "
                                + response.statusCode()
                                + " "
                                + response.body());
            }

            long backoffMs = (1L << attempt) * 1000;

            System.err.printf(
                    "OpenAI 호출 실패 (status=%d), %dms 후 재시도 (%d/%d)%n",
                    response.statusCode(),
                    backoffMs,
                    attempt,
                    MAX_RETRIES
            );

            if (attempt < MAX_RETRIES) {
                Thread.sleep(backoffMs);
            }
        }

        throw new IOException("OpenAI API 호출 재시도 초과");
    }

    private HttpRequest createRequest(String systemPrompt, String userPrompt) {

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.4);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.add(objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", userPrompt));

        requestBody.set("messages", messages);

        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(RESPONSE_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
    }

    private BriefingResult parseResponse(HttpResponse<String> response)
            throws IOException {

        JsonNode root = objectMapper.readTree(response.body());

        String text = root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();

        String actualModel = root.path("model").asText(model);

        return new BriefingResult(text, actualModel);
    }
}
