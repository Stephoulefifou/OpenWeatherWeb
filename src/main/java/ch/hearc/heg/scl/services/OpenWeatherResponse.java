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
        private Double lon;
        private Double lat;
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
        private Double temp;
        @SerializedName("feels_like")
        private Double feelsLike;
        @SerializedName("temp_min")
        private Double tempMin;
        @SerializedName("temp_max")
        private Double tempMax;
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
        private Double speed;
        private int deg;
        private Double gust;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rain {
        @SerializedName("1h")
        private Double oneHour;
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
