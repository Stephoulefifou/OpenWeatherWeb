package ch.hearc.heg.scl.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class CountryApi {
    private final HttpClient client;
    private final Gson gson;
    public CountryApi(){
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();}

    /**
     * Recherche du nom du pays par le code donné par OpenWeatherMap
     */
    public String getCountryName(String code){
        String url = "https://db.ig.he-arc.ch/ens/scl/ws/country/"+ code;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> countryMap = gson.fromJson(response.body(), type);
                return countryMap.get("name");
            } else {
                return "Erreur API : " + response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            return "Erreur lors de l'appel API : " + e.getMessage();
        }
    }
}
