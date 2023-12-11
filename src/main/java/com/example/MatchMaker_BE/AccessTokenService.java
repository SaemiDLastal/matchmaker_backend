package com.example.MatchMaker_BE;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;

@Service
public class AccessTokenService {

    /*@Value("${authorization-uri}")*/
    private String authURI = "https://auth-ger.bullhornstaffing.com/oauth/authorize";

    @Value("${client-id}")
    private String clientID;

    private final String username = System.getenv("API_USERNAME");

    private final String password = System.getenv("API_PASSWORD");

    private final RestTemplate restTemplate = new RestTemplate();
/*
    @Bean
    @PostMapping("/match")
    public String getAccessToken() {

        String accessToken = "test";
        /*String accessToken = restTemplate.getForObject(
                authURI + "?client_id=" + clientID + "&response_type=code&action=Login&username=" + username + "&password=" + password, //API Url zusammensetzen
                String.class
        );*/
/*
        System.out.println("accessToken: " + accessToken);
        return accessToken;
    }*/

    @Bean
    @PostMapping("/match")
    public String getAccessToken() {

        RestTemplate restTemplate = new RestTemplate(getCustomHttpRequestFactory());
        //HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON);
        //String requestBody = "{\"client_id\":\"" + clientID + "\"response_type\":\"" + "\"code\"" + "\",\"action\":\"Login\"" + "\",\"username\":\"" + username + "\",\"password\":\"" + password + "}";
        String requestURL = authURI + "?client_id=" + clientID + "&response_type=code&action=Login&username=" + username + "&password=" + password;
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(requestURL, String.class);
        String responseUrl = responseEntity.getHeaders().getFirst("Location");
        System.out.println("API Response URL: " + responseUrl);
        /*
        String accessToken = restTemplate.getForObject(
                authURI + "?client_id=" + clientID + "&response_type=code&action=Login&username=" + username + "&password=" + password, //API Url zusammensetzen
                String.class
        );*/

        return "Lets go";
    }

    private static ClientHttpRequestFactory getCustomHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                // Disable automatic redirects
                connection.setInstanceFollowRedirects(false);
            }
        };
        return requestFactory;
    }
}
