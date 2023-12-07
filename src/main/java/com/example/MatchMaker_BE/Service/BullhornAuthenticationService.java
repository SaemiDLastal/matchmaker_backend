package com.example.MatchMaker_BE.Service;

import com.github.scribejava.apis.GenericApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BullhornAuthenticationService {
   @Value("${oauth2.clientId}")
   private String clientId;

    @Value("${oauth2.clientSecret}")
    private String clientSecret;

    @Value("${oauth2.authorizationUrl}")
    private String authorizationUrl;

    @Value("${oauth2.accessTokenUrl}")
    private String accessTokenUrl;

    @Value("${oauth2.callbackUrl}")
    private String callbackUrl;

    @Bean
    public OAuth20Service getOAuth20Service() {
        return new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .defaultScope("openid")
                .callback(callbackUrl)
                .build(GenericApi20.instance());
    }

}

/*@Service
public class BullhornAuthenticationService {
    @Autowired
    private RestTemplate restTemplate;

    public String authenticate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        // Make a POST request to get the access token
        String tokenUrl = "https://auth.bullhorn.com/oauth/token"; // Or use a constant from configuration
        BullhornAccessTokenResponse response = restTemplate.postForObject(tokenUrl, request, BullhornAccessTokenResponse.class);

        if (response != null) {
            return response.getAccessToken();
        } else {
            return null; // Handle error condition appropriately
        }
    }
}*/

