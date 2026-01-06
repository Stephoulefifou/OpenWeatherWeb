package ch.hearc.heg.scl.services;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenWeatherResponse {

    private Coord coord;
    private List<Weather> weather;
    private String base;
    private Main main;
    private int visibility;
    private Wind wind;
    private Rain rain;
    private Clouds clouds;
    private long dt;
    private Sys sys;
    private int timezone;
    private int id;
    private String name;
    private int cod;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coord {
        private double lon;
        private double lat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Main {
        private double temp;
        @SerializedName("feels_like")
        private double feelsLike;
        @SerializedName("temp_min")
        private double tempMin;
        @SerializedName("temp_max")
        private double tempMax;
        private int pressure;
        private int humidity;
        @SerializedName("sea_level")
        private int seaLevel;
        @SerializedName("grnd_level")
        private int grndLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wind {
        private double speed;
        private int deg;
        private double gust;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rain {
        @SerializedName("1h")
        private double oneHour;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clouds {
        private int all;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sys {
        private int type;
        private long id;
        private String country;
        private long sunrise;
        private long sunset;
    }
}
