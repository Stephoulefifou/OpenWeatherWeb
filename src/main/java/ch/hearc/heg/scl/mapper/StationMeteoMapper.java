package ch.hearc.heg.scl.mapper;

import ch.hearc.heg.scl.business.Meteo;
import ch.hearc.heg.scl.business.Pays;
import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.services.OpenWeatherResponse;
import lombok.ToString;
import lombok.extern.java.Log;
import org.hibernate.Session;
import org.hibernate.query.Query;

@ToString
@Log
public class StationMeteoMapper {

    private final OpenWeatherResponse dto;
    private final Meteo meteo;
    private final Pays pays;

    public StationMeteoMapper(OpenWeatherResponse dto, Meteo meteo, Pays pays) {
        this.dto = dto;
        this.meteo = meteo;
        this.pays = pays;
    }

    public StationMeteo mapToEntity() {
        StationMeteo station = new StationMeteo();
        station.setNom(dto.getName());
        station.setPays(pays);
        station.setLongitude(dto.getCoord().getLon());
        station.setLatitude(dto.getCoord().getLat());
        station.setOpenWeatherMapId((int) dto.getId());
        station.getDonneesMeteo().add(meteo);
        return station;
    }

    public void insert(Session session, StationMeteo station) {
        session.persist(station);
        session.flush(); // récupère NUMERO généré par Oracle et met à jour FK Meteo
    }
}
