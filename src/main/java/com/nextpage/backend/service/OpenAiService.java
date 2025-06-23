package com.nextpage.backend.service;

import com.nextpage.backend.error.exception.openAI.OpenAiClientException;
import com.nextpage.backend.error.exception.openAI.OpenAiResponseException;
import com.nextpage.backend.error.exception.openAI.OpenAiServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final WebClient webClient;
    private final String apiKey;

    @Autowired
    public OpenAiService(WebClient.Builder webClientBuilder, @Value("${openai.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/").build();
        this.apiKey = apiKey;
    }
    public String generateImage(String content) {
        Map<String, Object> requestBody = prepareRequestBody(content);
        return this.webClient.post()
                .uri("/images/generations")
                .header("Authorization", "Bearer " + this.apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> Mono.error(new OpenAiClientException()))
                .onStatus(status -> status.is5xxServerError(), response -> Mono.error(new OpenAiServerException()))
                .bodyToMono(Map.class)
                .map(this::extractImageUrl)
                .block(); // This makes the call synchronous
    }


    private Map<String, Object> prepareRequestBody(String content) {
        String prompt = String.format("When generating an image, be sure to observe the following conditions: Do not add text to the image. I want an illustration image, not contain text in the image\nDesign: a detailed digital illustration drawn with bright colors and clean lines. Please make the following images according to the previous requirements: %s \n Avoid any content that may be considered inappropriate or offensive, ensuring the image aligns with content policies.", content);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");
        requestBody.put("model", "dall-e-3");
        return requestBody;
    }

    private String extractImageUrl(Map<String, Object> response) {
        if (response.containsKey("data")) {
            List<Map<String, Object>> images = (List<Map<String, Object>>) response.get("data");
            return images.stream()
                    .findFirst()
                    .map(image -> (String) image.get("url"))
                    .orElse("Image generation failed");
        }
            throw new OpenAiResponseException();
    }
}