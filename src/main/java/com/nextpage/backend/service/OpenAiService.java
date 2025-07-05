package com.nextpage.backend.service;

import com.nextpage.backend.error.exception.image.ImageDownloadException;
import com.nextpage.backend.error.exception.image.ImageUploadException;
import com.nextpage.backend.error.exception.openAI.OpenAiClientException;
import com.nextpage.backend.error.exception.openAI.OpenAiResponseException;
import com.nextpage.backend.error.exception.openAI.OpenAiServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAiService {

    private final WebClient.Builder webClientBuilder;
    private final ImageService imageService;

    @Value("${openai.api.key}")
    private String apiKey;

    /**
     * 1) DALL·E에서 이미지 생성
     * 2) S3 업로드 + Lambda 리사이징
     * 3) 리사이즈된 최종 URL 반환
     */
    public String generateImage(String content) {
        WebClient webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
        Map<String, Object> requestBody = prepareRequestBody(content);

        Map<String, Object> responseMap = webClient.post()
                .uri("/images/generations")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .exchangeToMono(this::handleResponse)
                .block();

        String dalleUrl = extractImageUrl(responseMap);

        try {
            return imageService.uploadWithLambda(dalleUrl);
        } catch (ImageDownloadException | ImageUploadException e) {
            log.error("이미지 처리 오류: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }

    private Mono<Map<String, Object>> handleResponse(ClientResponse resp) {
        if (resp.statusCode().is4xxClientError()) {
            return resp.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("OpenAI 4xx error: {} - {}", resp.statusCode(), body);
                        return Mono.error(new OpenAiClientException("OpenAI client error: " + body));
                    });
        } else if (resp.statusCode().is5xxServerError()) {
            return resp.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("OpenAI 5xx error: {} - {}", resp.statusCode(), body);
                        return Mono.error(new OpenAiServerException());
                    });
        } else {
            return resp.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
        }
    }

    private Map<String, Object> prepareRequestBody(String content) {
        String prompt = String.format(
                "When generating an image, observe: no text in image; illustration only. %s", content
        );
        Map<String, Object> req = new HashMap<>();
        req.put("prompt", prompt);
        req.put("n", 1);
        req.put("size", "1024x1024");
        req.put("model", "dall-e-2");
        return req;
    }

    @SuppressWarnings("unchecked")
    private String extractImageUrl(Map<String, Object> response) {
        if (response.containsKey("data")) {
            List<Map<String, Object>> images = (List<Map<String, Object>>) response.get("data");
            return images.stream()
                    .findFirst()
                    .map(img -> (String) img.get("url"))
                    .orElseThrow(OpenAiResponseException::new);
        }
        throw new OpenAiResponseException();
    }
}
