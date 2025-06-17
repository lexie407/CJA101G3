package com.toiukha.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConversationController {

    @Value("${dify.api.key}")
    private String difyApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/chat")
    public SseEmitter proxyToDify(@RequestBody Map<String, Object> requestBody, HttpServletRequest httpRequest) {
        SseEmitter emitter = new SseEmitter();

        String difyUrl = "https://api.dify.ai/v1/chat-messages";

        Map<String, Object> payload = new HashMap<>();
        payload.put("query", requestBody.get("query"));
        payload.put("inputs", new HashMap<>());
        payload.put("response_mode", "streaming");
        payload.put("conversation_id", requestBody.get("conversation_id"));
        payload.put("user", httpRequest.getSession().getId());

        restTemplate.execute(difyUrl, HttpMethod.POST, (ClientHttpRequest req) -> {
            HttpHeaders headers = req.getHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(difyApiKey);
            headers.setAccept(MediaType.parseMediaTypes("text/event-stream"));

            objectMapper.writeValue(req.getBody(), payload);
        }, (ClientHttpResponse res) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.getBody()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    emitter.send(line);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return null;
        });

        return emitter;
    }
}
