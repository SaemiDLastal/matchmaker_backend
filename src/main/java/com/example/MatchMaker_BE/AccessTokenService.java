package com.example.MatchMaker_BE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Service
public class AccessTokenService {

    /*@Value("${authorization-uri}")*/
    private String authURI = "https://auth-ger.bullhorn.com/oauth/authorize";

    @Value("${client-id}")
    private String clientID;

    private final String username = System.getenv("API_USERNAME");

    private final String password = System.getenv("API_PASSWORD");

    private final RestTemplate restTemplate = new RestTemplate();

    @Bean
    @PostMapping("/")
    public String getAccessToken() {
        String accessToken = "test";
        /*String accessToken = restTemplate.getForObject(
                authURI + "?client_id=" + clientID + "&response_type=code&action=Login&username=" + username + "&password=" + password, //API Url zusammensetzen
                String.class
        );*/

        System.out.println("accessToken: " + accessToken);
        return accessToken;
    }
}
