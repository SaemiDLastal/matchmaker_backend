package com.example.MatchMaker_BE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;

@Service
public class AccessTokenService {

    /*@Value("${authorization-uri}")*/
    private final String authURI = "https://auth-ger.bullhornstaffing.com/oauth/authorize";

    private final String accessURI = "https://auth-ger.bullhornstaffing.com/oauth/token";

    private String authorizationCode;

    @Value("${client-id}")
    private String clientID;

    @Value("${client-secret}")
    private String clientSecret;

    private final String username = System.getenv("API_USERNAME");

    private final String password = System.getenv("API_PASSWORD");

    @Bean
    @RequestMapping("/match")
    public String getAuthCode() {

        RestTemplate restTemplate = new RestTemplate(getCustomHttpRequestFactory());
        try {
            // Get the authorization code
            String fullAuthURL = authURI + "?client_id=" + clientID + "&response_type=code&action=Login&username=" + username + "&password=" + password;
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(fullAuthURL, String.class);
            String responseUrl = responseEntity.getHeaders().getFirst("Location");

            // Splitting the responseURL to extract the authorization code
            String[] responseUrlParts = responseUrl.split("code=");
            String[] responseUrlParts2 = responseUrlParts[1].split("&");
            String authorizationCode = responseUrlParts2[0];
            System.out.println("Authorization code: " + authorizationCode);

            return getAccessToken(authorizationCode);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public String getAccessToken(String authorizationCode) {

        try {
            RestTemplate restTemplate2 = new RestTemplate();
            String fullAccessURL = accessURI + "?grant_type=authorization_code&code=" + authorizationCode + "&client_id=" + clientID + "&client_secret=" + clientSecret;
            System.out.println("Full access URL: " + fullAccessURL);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            ResponseEntity<String> responseEntity2 = restTemplate2.exchange(fullAccessURL, HttpMethod.POST, entity, String.class);
            System.out.println("Response: " + responseEntity2);
            String accessToken = responseEntity2.getBody();
            System.out.println("Access token: " + accessToken);

            return accessToken;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    private static ClientHttpRequestFactory getCustomHttpRequestFactory() {
        return new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                // Disable automatic redirects
                connection.setInstanceFollowRedirects(false);
            }
        };
    }
}
