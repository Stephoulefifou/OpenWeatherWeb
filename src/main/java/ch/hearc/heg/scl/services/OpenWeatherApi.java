package ch.hearc.heg.scl.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class OpenWeatherApi {
    private final String apiKey;
    private final HttpClient client;

    public OpenWeatherApi() {
        this.apiKey = "29923a3ac413f437ec341af143debf45";
        this.client = HttpClient.newHttpClient();
    }
    /**
     * Appel de l'API avec comme paramètre l'URL créé en fonction de si on veut une recherche par coordonnées ou par station donnée.
     */
    private String call(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return (response.statusCode() == 200)
                    ? response.body()
                    : "Erreur API : " + response.statusCode();

        } catch (IOException | InterruptedException e) {
            return "Erreur lors de l'appel API : " + e.getMessage();
        }
    }

    /**
     * Création de l'URL à envoyer à l'API par coordonnée
     */
    public String callApi(double latitude, double longitude) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lang=fr&lat=%f&lon=%f&appid=%s&units=metric",
                latitude, longitude, apiKey
        );
        return call(url);
    }

    /**
     * Création de l'URL à envoyer à l'API par station (OpenWeatherMap ID de la station)
     */
    public String callApi(int owmId) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lang=fr&id=%d&appid=%s&units=metric",
                owmId, apiKey
        );
        return call(url);
    }
}

