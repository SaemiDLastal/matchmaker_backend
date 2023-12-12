package com.example.MatchMaker_BE;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//TODO Das hier muss noch implementiert werden: https://docs.spring.io/spring-security/reference/reactive/oauth2/client/core.html - Ist nur die Frage wo und wie...

@RestController
public class TokenRequestController {

    @Autowired
    AccessTokenService service;

    @RequestMapping("/match")
    @ResponseBody
    public String getAccessToken() {
        String accessToken = service.getAuthCode();
        return accessToken;
    }
}
