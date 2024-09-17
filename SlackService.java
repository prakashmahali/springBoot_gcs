package com.example.slackbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackService {

    @Value("${slack.bot.token}")
    private String slackBotToken;

    private static final String SLACK_URL = "https://slack.com/api/chat.postMessage";

    public void sendMessage(String channel, String message) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + slackBotToken);
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("channel", channel);
        body.put("text", message);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(SLACK_URL, HttpMethod.POST, entity, String.class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Message sent successfully!");
        } else {
            System.out.println("Failed to send message: " + response.getBody());
        }
    }
}
