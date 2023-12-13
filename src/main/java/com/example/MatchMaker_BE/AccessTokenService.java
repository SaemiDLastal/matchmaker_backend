package com.example.MatchMaker_BE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.io.IOException;
import java.net.HttpURLConnection;

@Service
public class AccessTokenService {

    /*@Value("${authorization-uri}")*/
    private final String authURI = "https://auth-ger.bullhornstaffing.com/oauth/authorize";

    private final String accessURI = "https://auth-ger.bullhornstaffing.com/oauth/token";

    private final String tokenURI = "https://rest-ger.bullhornstaffing.com/rest-services/login";

    private String authorizationCode;

    @Value("${client-id}")
    private String clientID;

    @Value("${client-secret}")
    private String clientSecret;

    private final String username = System.getenv("API_USERNAME");

    private final String password = System.getenv("API_PASSWORD");

    @Bean //Das hier rausnehmen, wenn Methode nur bei Aufruf durch Frontend aufgerufen werden soll
    public List<String> getMatchData() {

        //Authentifizierung
        //String bhRestToken = getBhRestToken(getAccessToken(getAuthCode()));
        getAccessToken(getAuthCode());

        //List<String> placementData = getPlacementData(bhRestToken);


        return List.of("test1", "test2", "test3");
    }

    // ------- API Authorisierung -------

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

            return authorizationCode;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public String getAccessToken(String authorizationCode) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            String fullAccessURL = accessURI + "?grant_type=authorization_code&code=" + authorizationCode + "&client_id=" + clientID + "&client_secret=" + clientSecret;
            System.out.println("Full access URL: " + fullAccessURL);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(fullAccessURL, HttpMethod.POST, entity, String.class);
            System.out.println("Response: " + responseEntity);
            String accessToken = extractValueFromJson(responseEntity.getBody(), "access_token");
            System.out.println("Access token: " + accessToken);

            return accessToken;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public String getBhRestToken(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();
        String fullTokenURL = tokenURI + "?version=*&access_token=" + accessToken;
        System.out.println("Full token URL: " + fullTokenURL);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(fullTokenURL, null, String.class);
        System.out.println("Response: " + responseEntity);
        String bhRestToken = extractValueFromJson(responseEntity.getBody(), "BhRestToken");
        System.out.println("BhRestToken: " + bhRestToken);
        return bhRestToken;
    }

    // ------- Datenabfragen -------

    public List<String> getPlacementData(String bhRestToken) {

            RestTemplate restTemplate = new RestTemplate();
            String fullPlacementURL = "https://rest-ger.bullhornstaffing.com/rest-services/Placement/Placement?BhRestToken=" + bhRestToken + "&fields=candidate(id(customText5),firstName,lastName),correlatedCustomText1,customText18,salaryUnit,payRate,clientBillRate,customTextBlock2,owner,dateBegin,dateEnd,jobOrder(clientCorporation(name,address))";
            System.out.println("Full placement URL: " + fullPlacementURL);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(fullPlacementURL, String.class);
            System.out.println("Response: " + responseEntity);
            String candidateFirstName = extractValueFromJson(responseEntity.getBody(), "firstName");
            String candidatelastName = extractValueFromJson(responseEntity.getBody(), "lastName");
            String candidateGesellschaft = extractValueFromJson(responseEntity.getBody(), "customText5");
            String zahlungszielPP = extractValueFromJson(responseEntity.getBody(), "correlatedCustomText1");
            String zahlungszielKunde = extractValueFromJson(responseEntity.getBody(), "customText18");
            String vergutungsart = extractValueFromJson(responseEntity.getBody(), "salaryUnit");
            String ek = extractValueFromJson(responseEntity.getBody(), "payRate");
            String vk = extractValueFromJson(responseEntity.getBody(), "clientBillRate");
            String aufgabenbeschreibung = extractValueFromJson(responseEntity.getBody(), "customTextBlock2");
            String ownerFirstName = extractValueFromJson(responseEntity.getBody(), "owner(firstName)");
            String ownerLastName = extractValueFromJson(responseEntity.getBody(), "owner(lastName)");
            String dateBegin = extractValueFromJson(responseEntity.getBody(), "dateBegin");
            String dateEnd = extractValueFromJson(responseEntity.getBody(), "dateEnd");
            String ppPosition = extractValueFromJson(responseEntity.getBody(), "jobOrder(title)");
            String corporateName = extractValueFromJson(responseEntity.getBody(), "jobOrder(clientCorporation(name))");
            String corporateAddress = extractValueFromJson(responseEntity.getBody(), "jobOrder(clientCorporation(address))");

            return List.of("test1", "test2", "test3");
    }


    // ------ Helper methods ------

    //Für Abfrage des authCodes aus der URL
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

    //Extrahiert den Wert eines bestimmten Feldes aus einem JSON-String
    public String extractValueFromJson(String jsonResponse, String fieldName) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonResponse);
            JsonNode fieldValue = jsonNode.get(fieldName);

            if(fieldValue != null && !fieldValue.isNull()) {
                return fieldValue.asText();
            } else {
                return "Wert nicht gefunden";
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Fehler beim Parsen der JSON";
        }

    }
}
