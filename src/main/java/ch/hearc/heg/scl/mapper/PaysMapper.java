package ch.hearc.heg.scl.mapper;

import ch.hearc.heg.scl.business.Pays;
import ch.hearc.heg.scl.services.CountryApi;
import ch.hearc.heg.scl.services.OpenWeatherResponse;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class PaysMapper {

    private final OpenWeatherResponse dto;
    private final CountryApi countryApi;

    public PaysMapper(OpenWeatherResponse dto, CountryApi countryApi) {
        this.dto = dto;
        this.countryApi = countryApi;
    }

    /**
     * Transforme le DTO OpenWeatherResponse en entité Pays
     */
    public Pays mapToEntity() {
        String code = dto.getSys().getCountry();
        if (code == null || code.isBlank()) {
            code = "UNK"; // Unknown
        }
        String nom = countryApi.getCountryName(code); // ou code si API retourne null
        return Pays.builder()
                .code(code)
                .nom(nom)
                .build();
    }

    /**
     * Vérifie si le pays existe déjà dans la base
     */
    public boolean exists(Session session, Pays p) {
        String hql = "SELECT COUNT(p) FROM Pays p WHERE p.code = :code";
        Query<Long> query = session.createQuery(hql, Long.class);
        query.setParameter("code", p.getCode());
        Long count = query.uniqueResult();
        return count != null && count > 0;
    }

    /**
     * Persiste le pays dans la base si pas déjà présent
     */
    public Pays insert(Session session, Pays pays) {
        session.persist(pays);
        session.flush();       // récupère la valeur du trigger
        return pays;
    }
}
