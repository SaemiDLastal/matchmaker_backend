package com.example.MatchMaker_BE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//TODO Das hier muss noch implementiert werden: https://docs.spring.io/spring-security/reference/reactive/oauth2/client/core.html - Ist nur die Frage wo und wie...

@RestController
public class TokenRequestController {

    @Autowired
    AccessTokenService service;

    @RequestMapping("/match")
    @ResponseBody
    public List<String> getMatchData() {

        return service.getMatchData();
    }
}
