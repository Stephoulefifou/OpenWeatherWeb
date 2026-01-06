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

    /**
     * Transforme le DTO OpenWeatherResponse en entité Météo
     */
    public Meteo mapToEntity() {
        Meteo meteo = new Meteo();

        // --- Date et mesures ---
        meteo.setDateMesure(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(dto.getDt()),
                ZoneId.systemDefault()
        ));
        meteo.setTemperature(dto.getMain().getTemp());
        meteo.setPression(dto.getMain().getPressure());
        meteo.setHumidite(dto.getMain().getHumidity());
        meteo.setVisibilite(dto.getVisibility());
        if (dto.getRain() != null) {
            meteo.setPrecipitation(dto.getRain().getOneHour());
        }

        // --- Descriptions météo ---
        if (dto.getWeather() != null && !dto.getWeather().isEmpty()) {
            dto.getWeather().forEach(w -> {
                String desc = w.getDescription();
                if (desc == null || desc.isBlank()) {
                    desc = "Pas de description";
                }
                meteo.getTexte().add(desc);
            });
        }

        // --- Sécurité : jamais laisser la liste vide ---
        if (meteo.getTexte().isEmpty()) {
            meteo.getTexte().add("Pas de description");
        }

        return meteo;
    }
}
