package com.example.MatchMaker_BE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import jakarta.servlet.ServletOutputStream;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLOutput;
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

    //private final String clientID = System.getenv("CLIENT_ID");
    private final String clientID = "ac871643-a981-4e9a-a023-a11b8f1703a5";

    //private final String clientSecret = System.getenv("CLIENT_SECRET");
    private final String clientSecret = "nMAWJCqdFI864Sxa1dSxFri5";

    //private final String username = System.getenv("API_USERNAME");
    private final String username = "wematch.api";

    //private final String password = System.getenv("API_PASSWORD");
    private final String password = "G1raffe030!WeMatch";

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
            System.out.println("Full auth URL: " + fullAuthURL);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(fullAuthURL, String.class);
            String responseUrl = responseEntity.getHeaders().getFirst("Location");
            System.out.println("Response URL: " + responseUrl);

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

//TODO Ansprechpartner Kunde fehlt noch
    public List<String> getPlacementData(String bhRestToken) {

        RestTemplate restTemplate = new RestTemplate();
        String fullPlacementURL = "https://rest70.bullhornstaffing.com/rest-services/8WY9C4/entity/Placement/" + matchID + "?BhRestToken=" + bhRestToken + "&fields=candidate(id(customText5),firstName,lastName,email),correlatedCustomText1,customText18,salaryUnit,payRate,clientBillRate,customTextBlock2,owner(email),dateBegin,dateEnd,customText17,correlatedCustomText4,jobOrder(clientContact(firstName,lastName),clientCorporation(name,address(address1,address2,city,zip),customText12))";
        System.out.println("Full placement URL: " + fullPlacementURL);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(fullPlacementURL, String.class);
        System.out.println("Response: " + responseEntity);

        // -- Daten aus JSON-Response extrahieren --
        String candidateFirstName = extractCandidate(responseEntity.getBody(), "firstName");
        String candidatelastName = extractCandidate(responseEntity.getBody(), "lastName");
        String candidateGesellschaft = extractCandidate(responseEntity.getBody(), "customText5");
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
        String corporateAddressStreet = extractCompanyAddress(responseEntity.getBody(), "address1");
        String corporateAddressNr = extractCompanyAddress(responseEntity.getBody(), "address2");
        String corporateAddressCity = extractCompanyAddress(responseEntity.getBody(), "city");
        String corporateAddressZip = extractCompanyAddress(responseEntity.getBody(), "zip");
        String kundigungsfristPP = extractallOtherData(responseEntity.getBody(), "customText17");
        String kundigungsfristKunde = extractCompany(responseEntity.getBody(), "customText12");
        String einsatzort = extractallOtherData(responseEntity.getBody(), "correlatedCustomText4");
        String candidateEmail = extractCandidate(responseEntity.getBody(), "email");
        String ownerEmail = extractOwner(responseEntity.getBody(), "email");
        String clientContactFirstName = extractClientContact(responseEntity.getBody(), "firstName");
        String clientContactLastName = extractClientContact(responseEntity.getBody(), "lastName");

        // -- Daten auf null überprüfen und ggf. mit leerem String ersetzen --
         candidateFirstName = (candidateFirstName == null || candidateFirstName.equals("null") ? "" : candidateFirstName);
         candidatelastName = (candidatelastName == null || candidatelastName.equals("null") ? "" : candidatelastName);
         candidateGesellschaft = (candidateGesellschaft == null || candidateGesellschaft.equals("null") ? "" : candidateGesellschaft);
         zahlungszielPP = (zahlungszielPP == null || zahlungszielPP.equals("null") ? "" : zahlungszielPP);
         zahlungszielKunde =  (zahlungszielKunde == null || zahlungszielKunde.equals("null") ? "" : zahlungszielKunde);
         vergutungsart = (vergutungsart == null || vergutungsart.equals("null") ? "" : vergutungsart);
         ek = (ek == null || ek.equals("null") ? "" : ek);
         vk = (vk == null || vk.equals("null") ? "" : vk);
         aufgabenbeschreibung = (aufgabenbeschreibung == null || aufgabenbeschreibung.equals("null") ? "" : aufgabenbeschreibung);
         ownerFirstName = (ownerFirstName == null || ownerFirstName.equals("null") ? "" : ownerFirstName);
         ownerLastName = (ownerLastName == null || ownerLastName.equals("null") ? "" : ownerLastName);
         dateBegin = (dateBegin == null || dateBegin.equals("null") ? "" : dateBegin);
         dateEnd = (dateEnd == null || dateEnd.equals("null") ? "" : dateEnd);
         ppPosition = (ppPosition == null || ppPosition.equals("null") ? "" : ppPosition);
         corporateName = (corporateName == null || corporateName.equals("null") ? "" : corporateName);
         corporateAddressStreet = (corporateAddressStreet == null || corporateAddressStreet.equals("null") ? "" : corporateAddressStreet);
         corporateAddressNr = (corporateAddressNr == null || corporateAddressNr.equals("null") ? "" : corporateAddressNr);
         corporateAddressCity = (corporateAddressCity == null || corporateAddressCity.equals("null") ? "" : corporateAddressCity);
         corporateAddressZip = (corporateAddressZip == null || corporateAddressZip.equals("null") ? "" : corporateAddressZip);
         kundigungsfristPP = (kundigungsfristPP == null || kundigungsfristPP.equals("null") ? "" : kundigungsfristPP);
         kundigungsfristKunde = (kundigungsfristKunde == null || kundigungsfristKunde.equals("null") ? "" : kundigungsfristKunde);
         einsatzort = (einsatzort == null || einsatzort.equals("null") ? "" : einsatzort);
         candidateEmail = (candidateEmail == null || candidateEmail.equals("null") ? "" : candidateEmail);
         ownerEmail = (ownerEmail == null || ownerEmail.equals("null") ? "" : ownerEmail);
        clientContactFirstName = (clientContactFirstName == null || clientContactFirstName.equals("null") ? "" : clientContactFirstName);
        clientContactLastName = (clientContactLastName == null || clientContactLastName.equals("null") ? "" : clientContactLastName);

        return List.of(candidateFirstName, candidatelastName, candidateGesellschaft, zahlungszielPP, zahlungszielKunde, vergutungsart, ek, vk, aufgabenbeschreibung, ownerFirstName, ownerLastName, dateBegin, dateEnd, ppPosition, corporateName, corporateAddressStreet, corporateAddressNr, corporateAddressCity, corporateAddressZip, kundigungsfristPP, kundigungsfristKunde, einsatzort, candidateEmail, ownerEmail, clientContactFirstName, clientContactLastName);
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

    public static String extractCompanyAddress(String JsonResponse, String fieldName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(JsonResponse);

            JsonNode valueNode = jsonNode.path("data").path("jobOrder").path("clientCorporation").path("address").path(fieldName);
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

    public String extractClientContact(String JsonResponse, String fieldName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(JsonResponse);

            JsonNode valueNode = jsonNode.path("data").path("jobOrder").path("clientContact").path(fieldName);
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
