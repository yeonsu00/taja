package com.taja.simulator.infrastructure.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiContentAgent {

    private static final String GEMINI_MODEL = "gemini-2.5-flash-lite";

    private final Client client;
    private final boolean enabled;

    public AiContentAgent(@Value("${gemini.api-key:}") String apiKey) {
        this.enabled = apiKey != null && !apiKey.isBlank();
        if (this.enabled) {
            this.client = Client.builder().apiKey(apiKey).build();
            log.info("AI 콘텐츠 에이전트 활성화 (Gemini API)");
        } else {
            this.client = null;
            log.info("AI 콘텐츠 에이전트 비활성화 (API 키 없음) — 템플릿 콘텐츠 사용");
        }
    }

    public String generatePostContent(String personaName, String personaDescription, String stationName) {
        if (!enabled) {
            return personaName + "입니다. " + stationName + " 따릉이 이용했어요!";
        }
        String prompt = String.format(
                "서울 따릉이 서비스 사용자입니다.\n" +
                "페르소나: %s - %s\n" +
                "역 근처 따릉이 게시판에 올릴 자연스러운 한국어 게시글을 1~2문장으로 작성해주세요.\n" +
                "주의사항:\n" +
                "- 게시글 본문 텍스트만 출력하세요. 제목, 번호, 마크다운, 설명 등 일절 포함하지 마세요.\n" +
                "- 역 번호나 역 ID(예: 역588, 역1234)는 절대 언급하지 마세요.",
                personaName, personaDescription
        );
        try {
            return callGemini(prompt, 200);
        } catch (Exception e) {
            log.warn("Gemini API 호출 실패, 템플릿 사용: {}", e.getMessage());
            return personaName + "입니다. " + stationName + " 따릉이 이용했어요!";
        }
    }

    public String generateCommentContent(String personaName, String personaDescription) {
        if (!enabled) {
            return "유용한 정보 감사합니다!";
        }
        String prompt = String.format(
                "서울 따릉이 서비스 사용자입니다.\n" +
                "페르소나: %s - %s\n" +
                "따릉이 관련 게시글에 달 자연스러운 한국어 댓글을 1문장으로 작성해주세요.\n" +
                "주의사항:\n" +
                "- 댓글 본문 텍스트만 출력하세요. 번호, 마크다운, 설명 등 일절 포함하지 마세요.\n" +
                "- 역 번호나 역 ID(예: 역588, 역1234)는 절대 언급하지 마세요.",
                personaName, personaDescription
        );
        try {
            return callGemini(prompt, 100);
        } catch (Exception e) {
            log.warn("Gemini API 호출 실패, 템플릿 사용: {}", e.getMessage());
            return "좋은 정보네요!";
        }
    }

    private String callGemini(String prompt, int maxOutputTokens) {
        GenerateContentResponse response = client.models.generateContent(
                GEMINI_MODEL,
                prompt,
                GenerateContentConfig.builder().maxOutputTokens(maxOutputTokens).build()
        );
        String text = response.text();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini API 응답이 비어있음");
        }
        return text.trim();
    }
}
