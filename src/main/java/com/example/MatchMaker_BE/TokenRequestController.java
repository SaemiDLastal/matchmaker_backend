package com.example.MatchMaker_BE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
public class TokenRequestController {

    private String matchID;

    /*@Value("${authorization-uri}")*/
    private final String authURI = "https://auth-ger.bullhornstaffing.com/oauth/authorize";

    private final String accessURI = "https://auth-ger.bullhornstaffing.com/oauth/token";

    private final String tokenURI = "https://rest-ger.bullhornstaffing.com/rest-services/login";

    private String authorizationCode;

    private final String clientID = System.getenv("CLIENT_ID");

    //@Value("${client-secret}")
    private final String clientSecret = System.getenv("CLIENT_SECRET");

    private final String username = System.getenv("API_USERNAME");

    private final String password = System.getenv("API_PASSWORD");

    //@Bean //Das hier rausnehmen, wenn Methode nur bei Aufruf durch Frontend aufgerufen werden soll
    @GetMapping("/match/{localMatchID}")
    public String getMatchData(@PathVariable String localMatchID) {

        this.matchID = localMatchID;

        // API-Authentifizierung
        String bhRestToken = getBhRestToken(getAccessToken(getAuthCode()));
        List<String> placementData = getPlacementData(bhRestToken);
        System.out.println("PlacementData: " + placementData);

        return new Gson().toJson(placementData);
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
    public String getAccessToken(String authorizationCode){
        try {
            String fullAccessURL = accessURI + "?grant_type=authorization_code&code=" + authorizationCode + "&client_id=" + clientID + "&client_secret=" + clientSecret;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fullAccessURL)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String accessToken = extractValueFromJson(response.body(), "access_token");
                System.out.println("Access token: " + accessToken);
                return accessToken;
            } else {
                System.out.println("Error: " + response.statusCode() + ", " + response.body());
                return null;
            }
        } catch(Exception e) {
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

//TODO Ansprechpartner Kunde, Owner Email, Candidate Email, Kündigungsfrist, Einsatzort (Remote/On-site) fehlen noch
    public List<String> getPlacementData(String bhRestToken) {

        RestTemplate restTemplate = new RestTemplate();
        String fullPlacementURL = "https://rest70.bullhornstaffing.com/rest-services/8WY9C4/entity/Placement/" + matchID + "?BhRestToken=" + bhRestToken + "&fields=candidate(id(customText5),firstName,lastName),correlatedCustomText1,customText18,salaryUnit,payRate,clientBillRate,customTextBlock2,owner,dateBegin,dateEnd,jobOrder(clientCorporation(name,address))";
        System.out.println("Full placement URL: " + fullPlacementURL);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(fullPlacementURL, String.class);
        System.out.println("Response: " + responseEntity);

        String candidateFirstName = extractCandidate(responseEntity.getBody(), "firstName");

        String candidatelastName = extractCandidate(responseEntity.getBody(), "lastName");

        String candidateGesellschaft = extractCompany(responseEntity.getBody(), "name");

        String zahlungszielPP = extractallOtherData(responseEntity.getBody(), "correlatedCustomText1");

        String zahlungszielKunde = extractallOtherData(responseEntity.getBody(), "customText18");

        String vergutungsart = extractallOtherData(responseEntity.getBody(), "salaryUnit");

        String ek = extractallOtherData(responseEntity.getBody(), "payRate");

        String vk = extractallOtherData(responseEntity.getBody(), "clientBillRate");

        String aufgabenbeschreibung = extractallOtherData(responseEntity.getBody(), "customTextBlock2");

        String ownerFirstName = extractOwner(responseEntity.getBody(), "firstName");

        String ownerLastName = extractOwner(responseEntity.getBody(), "lastName");

        String dateBegin = extractallOtherData(responseEntity.getBody(), "dateBegin");

        String dateEnd = extractallOtherData(responseEntity.getBody(), "dateEnd");

        String ppPosition = extractJobOrderTitle(responseEntity.getBody(), "title");

        String corporateName = extractCompany(responseEntity.getBody(), "name");

        String corporateAddress = extractCompany(responseEntity.getBody(), "address");
        System.out.println("corporateAddress: " + corporateAddress.toString());

        return List.of(candidateFirstName, candidatelastName, candidateGesellschaft, zahlungszielPP, zahlungszielKunde, vergutungsart, ek, vk, aufgabenbeschreibung, ownerFirstName, ownerLastName, dateBegin, dateEnd, ppPosition, corporateName, corporateAddress.toString());
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
                //  return "Wert nicht gefunden";
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Fehler beim Parsen der JSON";
        }
        return "Fehler in extractValueFromJson";
    }

    public static String extractCandidate(String JsonResponse, String fieldName) {
        try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(JsonResponse);

        JsonNode valueNode = jsonNode.path("data").path("candidate").path(fieldName);
        if (valueNode.isMissingNode()) {
            // Hier kannst du entscheiden, wie du mit dem Fehlen des Feldes umgehen möchtest
            return null;
        }

        return valueNode.asText();
        } catch (Exception e) {
            e.printStackTrace(); // Hier kannst du die Exception-Verarbeitung entsprechend deinen Anforderungen anpassen
            return null;
        }
    }
    public static String extractCompany(String JsonResponse, String fieldName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(JsonResponse);

            JsonNode valueNode = jsonNode.path("data").path("jobOrder").path("clientCorporation").path(fieldName);
            if (valueNode.isMissingNode()) {
                return null;
            }
            return valueNode.asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public String extractallOtherData(String JsonResponse, String fieldName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(JsonResponse);

            JsonNode valueNode = jsonNode.path("data").path(fieldName);
            if (valueNode.isMissingNode()) {

                return null;
            }

            return valueNode.asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public String extractOwner(String JsonResponse, String fieldName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(JsonResponse);

            JsonNode valueNode = jsonNode.path("data").path("owner").path(fieldName);
            if (valueNode.isMissingNode()) {

                return null;
            }

            return valueNode.asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public String extractJobOrderTitle(String JsonResponse, String fieldName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(JsonResponse);

            JsonNode valueNode = jsonNode.path("data").path("jobOrder").path(fieldName);
            if (valueNode.isMissingNode()) {

                return null;
            }

            return valueNode.asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
