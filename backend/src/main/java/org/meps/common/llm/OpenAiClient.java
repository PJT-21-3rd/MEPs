package org.meps.common.llm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OpenAI Chat Completions 래퍼.
 * Lombok @RequiredArgsConstructor는 필드 @Qualifier를 생성자 파라미터로 복사하지 않아
 * (lombok.config 미설정) 명시적 생성자를 쓴다
 */
@Component
public class OpenAiClient {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final double TEMPERATURE = 0.3;
    private static final int MAX_TOKENS = 600;
    // GPT-5 계열은 reasoning 토큰이 completion 한도를 함께 소모 — 한도가 작으면 본문이
    // 비어 나온다(실측: low+1500, medium+4000에서 content 없음). 넉넉히 잡는다
    private static final int MAX_COMPLETION_TOKENS = 8000;

    private final RestTemplate llmRestTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public OpenAiClient(@Qualifier("llmRestTemplate") RestTemplate llmRestTemplate,
                        ObjectMapper objectMapper) {
        this.llmRestTemplate = llmRestTemplate;
        this.objectMapper = objectMapper;
    }

    /** building_safety_report.ai_model_nm 기록용 */
    public String getModel() {
        return model;
    }

    /** JSON 모드로 호출해 assistant 메시지 content(JSON 문자열)를 반환 */
    public String completeJson(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        if (model.startsWith("gpt-5")) {
            // GPT-5 계열: temperature 지정 불가(기본값 고정), max_tokens 대신 max_completion_tokens.
            // medium은 45초 읽기 타임아웃도 초과(실측). 규칙을 압축한 뒤로는 minimal로도 준수돼
            // 생성 지연 최소화 — 품질 저하가 보이면 low로 올릴 것
            body.put("max_completion_tokens", MAX_COMPLETION_TOKENS);
            body.put("reasoning_effort", "minimal");
        } else {
            body.put("temperature", TEMPERATURE);
            body.put("max_tokens", MAX_TOKENS);
        }

        try {
            ResponseEntity<String> response = llmRestTemplate.postForEntity(
                    CHAT_COMPLETIONS_URL, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                throw new LlmCallFailedException("OpenAI 응답에 content가 없음");
            }
            return content;
        } catch (LlmCallFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmCallFailedException("OpenAI 호출 실패", e);
        }
    }
}
