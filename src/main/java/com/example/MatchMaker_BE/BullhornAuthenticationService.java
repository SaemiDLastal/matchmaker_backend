package com.example.MatchMaker_BE;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
/*
@Service
public class BullhornAuthenticationService {

    private RestTemplate restTemplate;

    public String authenticate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        // Make a POST request to get the access token
        String tokenUrl = "https://auth.bullhorn.com/oauth/token"; // Or use a constant from configuration
        String response = restTemplate.postForObject(tokenUrl, request, String.class);

        if (response != null) {
            return response.getAccessToken();
        } else {
            return null; // Handle error condition appropriately
        }
    }
}*/

