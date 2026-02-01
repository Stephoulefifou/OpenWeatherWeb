package ch.hearc.heg.scl.mapper;

import ch.hearc.heg.scl.business.Meteo;
import ch.hearc.heg.scl.services.OpenWeatherResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MeteoMapper {

    private final OpenWeatherResponse dto;

    public MeteoMapper(OpenWeatherResponse dto) {
        this.dto = dto;
    }

    public Meteo mapToEntity() {
        Meteo meteo = new Meteo();

        // --- Date de la mesure ---
        meteo.setDateMesure(toLocalDateTime(dto.getDt()));

        // --- Températures (Main) ---
        if (dto.getMain() != null) {
            meteo.setTemperature(dto.getMain().getTemp());
            meteo.setRessenti(dto.getMain().getFeelsLike());
            meteo.setTempMin(dto.getMain().getTempMin());
            meteo.setTempMax(dto.getMain().getTempMax());
            meteo.setPression(dto.getMain().getPressure());
            meteo.setHumidite(dto.getMain().getHumidity());
        }

        // --- Divers ---
        meteo.setVisibilite(dto.getVisibility());

        // --- Pluie ---
        if (dto.getRain() != null) {
            meteo.setPrecipitation(dto.getRain().getOneHour());
        }

        // --- Vent ---
        if (dto.getWind() != null) {
            meteo.setVentVitesse(dto.getWind().getSpeed());
            meteo.setVentDirection(dto.getWind().getDeg());
            meteo.setVentRafales(dto.getWind().getGust());
        }

        // --- Soleil (Sys) ---
        if (dto.getSys() != null) {
            meteo.setLeverSoleil(toLocalDateTime(dto.getSys().getSunrise()));
            meteo.setCoucherSoleil(toLocalDateTime(dto.getSys().getSunset()));
        }

        // --- Descriptions ---
        if (dto.getWeather() != null && !dto.getWeather().isEmpty()) {
            dto.getWeather().forEach(w -> {
                String desc = w.getDescription();
                meteo.getTexte().add((desc == null || desc.isBlank()) ? "Pas de description" : desc);
            });
        }

        if (meteo.getTexte().isEmpty()) {
            meteo.getTexte().add("Pas de description");
        }

        return meteo;
    }

    /**
     * Utilitaire pour convertir un timestamp Unix (long) en LocalDateTime
     */
    private LocalDateTime toLocalDateTime(long unixTimestamp) {
        if (unixTimestamp <= 0) return null;
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(unixTimestamp),
                ZoneId.systemDefault()
        );
    }
}