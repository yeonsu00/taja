package com.taja.simulator.infrastructure.client;

import com.taja.simulator.config.SimulatorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TajaApiClient {

    private final WebClient webClient;
    private static final String PASSWORD = "test1234";

    private static final List<String> SEARCH_KEYWORDS = List.of(
            "광화문", "홍대", "강남", "신촌", "이태원", "합정", "건대", "성수", "망원", "여의도"
    );

    public TajaApiClient(WebClient.Builder builder, SimulatorProperties properties) {
        this.webClient = builder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public boolean signup(String name, String email) {
        try {
            webClient.post()
                    .uri("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", name, "email", email, "password", PASSWORD))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("회원가입 실패: {}", e.getMessage());
            return false;
        }
    }

    public Optional<String> login(String email) {
        try {
            Map<?, ?> response = webClient.post()
                    .uri("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("email", email, "password", PASSWORD))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("data") instanceof Map<?, ?> data) {
                return Optional.ofNullable((String) data.get("accessToken"));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.debug("로그인 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<Long> searchStations() {
        try {
            String keyword = SEARCH_KEYWORDS.get(new Random().nextInt(SEARCH_KEYWORDS.size()));
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/stations/map/search")
                            .queryParam("keyword", keyword)
                            .queryParam("lat", 37.5665)
                            .queryParam("lng", 126.9780)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("data") instanceof List<?> list) {
                return list.stream()
                        .filter(item -> item instanceof Map<?, ?> m && m.get("stationId") instanceof Number)
                        .map(item -> ((Number) ((Map<?, ?>) item).get("stationId")).longValue())
                        .collect(Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            log.debug("역 검색 실패: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean joinBoard(Long stationId, String accessToken) {
        try {
            webClient.post()
                    .uri("/stations/{stationId}/posts/join", stationId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("게시판 참여 실패 (stationId: {}): {}", stationId, e.getMessage());
            return false;
        }
    }

    public boolean createPost(Long stationId, String content, String accessToken) {
        try {
            webClient.post()
                    .uri("/stations/{stationId}/posts", stationId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("content", content))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("게시글 작성 실패 (stationId: {}): {}", stationId, e.getMessage());
            return false;
        }
    }

    public Optional<Long> getLatestPostId(Long stationId) {
        try {
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/stations/{stationId}/posts")
                            .queryParam("sort", "LATEST")
                            .queryParam("size", 1)
                            .build(stationId))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null
                    && response.get("data") instanceof Map<?, ?> data
                    && data.get("posts") instanceof List<?> posts
                    && !posts.isEmpty()
                    && posts.get(0) instanceof Map<?, ?> post
                    && post.get("postId") instanceof Number postId) {
                return Optional.of(postId.longValue());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.debug("최신 게시글 조회 실패 (stationId: {}): {}", stationId, e.getMessage());
            return Optional.empty();
        }
    }

    public boolean createComment(Long postId, String content, String accessToken) {
        try {
            webClient.post()
                    .uri("/posts/{postId}/comments", postId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("content", content))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("댓글 작성 실패 (postId: {}): {}", postId, e.getMessage());
            return false;
        }
    }

    public boolean likePost(Long postId, String accessToken) {
        try {
            webClient.post()
                    .uri("/posts/{postId}/like", postId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("좋아요 실패 (postId: {}): {}", postId, e.getMessage());
            return false;
        }
    }

    public boolean addFavorite(Long stationId, String accessToken) {
        try {
            webClient.post()
                    .uri("/stations/{stationId}/favorite", stationId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("즐겨찾기 추가 실패 (stationId: {}): {}", stationId, e.getMessage());
            return false;
        }
    }
}
