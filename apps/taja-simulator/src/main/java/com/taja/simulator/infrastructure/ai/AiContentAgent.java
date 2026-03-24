package com.taja.simulator.infrastructure.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiContentAgent {

    private final AnthropicClient client;
    private final boolean enabled;

    public AiContentAgent(@Value("${anthropic.api-key:}") String apiKey) {
        this.enabled = apiKey != null && !apiKey.isBlank();
        if (this.enabled) {
            this.client = AnthropicOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
            log.info("AiContentAgent enabled with Claude API");
        } else {
            this.client = null;
            log.info("AiContentAgent disabled (no API key) — using template content");
        }
    }

    public String generatePostContent(String personaName, String personaDescription, String stationName) {
        if (!enabled) {
            return personaName + "입니다. " + stationName + " 따릉이 이용했어요!";
        }
        try {
            String prompt = String.format(
                    "서울 따릉이 서비스 사용자입니다.\n" +
                    "페르소나: %s - %s\n" +
                    "역 이름: %s\n" +
                    "이 역 게시판에 올릴 자연스러운 한국어 게시글을 1~2문장으로만 작성해주세요.",
                    personaName, personaDescription, stationName
            );

            var message = client.messages().create(
                    MessageCreateParams.builder()
                            .model("claude-haiku-4-5-20251001")
                            .maxTokens(200)
                            .addUserMessage(prompt)
                            .build()
            );

            return message.content().stream()
                    .filter(c -> c.isText())
                    .map(c -> c.asText().text())
                    .findFirst()
                    .orElse(personaName + "입니다. " + stationName + " 이용 좋았어요!");
        } catch (Exception e) {
            log.warn("Claude API call failed, using template: {}", e.getMessage());
            return personaName + "입니다. " + stationName + " 따릉이 이용했어요!";
        }
    }

    public String generateCommentContent(String personaName, String personaDescription) {
        if (!enabled) {
            return "유용한 정보 감사합니다!";
        }
        try {
            String prompt = String.format(
                    "서울 따릉이 서비스 사용자입니다.\n" +
                    "페르소나: %s - %s\n" +
                    "따릉이 관련 게시글에 달 자연스러운 한국어 댓글을 1문장으로만 작성해주세요.",
                    personaName, personaDescription
            );

            var message = client.messages().create(
                    MessageCreateParams.builder()
                            .model("claude-haiku-4-5-20251001")
                            .maxTokens(100)
                            .addUserMessage(prompt)
                            .build()
            );

            return message.content().stream()
                    .filter(c -> c.isText())
                    .map(c -> c.asText().text())
                    .findFirst()
                    .orElse("좋은 정보네요!");
        } catch (Exception e) {
            log.warn("Claude API call failed, using template: {}", e.getMessage());
            return "좋은 정보네요!";
        }
    }
}
