package com.example.MatchMaker_BE;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Service
public class AccessTokenService {

    /*@Value("${authorization-uri}")*/
    private final String authURI = "https://auth-ger.bullhornstaffing.com/oauth/authorize";

    private final String accessURI = "https://auth-ger.bullhornstaffing.com/oauth/token";

    @Value("${client-id}")
    private String clientID;

    @Value("${client-secret}")
    private String clientSecret;

    private final String username = System.getenv("API_USERNAME");

    private final String password = System.getenv("API_PASSWORD");

    @Bean
    @RequestMapping("/match")
    public String getAccessToken() {

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

            // Get the access token
            /*
            RestTemplate restTemplate2 = new RestTemplate(getCustomHttpRequestFactory());
            String fullAccessURL = accessURI + "?grant_type=authorization_code&code=" + authorizationCode + "&client_id=" + clientID + "&client_secret=" + clientSecret;
            System.out.println("Full access URL: " + fullAccessURL);
            ResponseEntity<String> responseEntity2 = restTemplate2.postForEntity(fullAccessURL, null, String.class);
            String accessToken = responseEntity2.getBody();
            System.out.println("Access token: " + accessToken);
 */
            RestTemplate restTemplate2 = new RestTemplate();
            String fullAccessURL = accessURI + "?grant_type=authorization_code&code=" + authorizationCode + "&client_id=" + clientID + "&client_secret=" + clientSecret;
            System.out.println("Full access URL: " + fullAccessURL);
            HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            ResponseEntity<String> responseEntity2 = restTemplate2.exchange(fullAccessURL, HttpMethod.POST, entity, String.class);
            System.out.println("Response: " + responseEntity2);
            String accessToken = responseEntity2.getBody();
            System.out.println("Access token: " + accessToken);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return "Lets go";
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
