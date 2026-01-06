package ch.hearc.heg.scl.services;

import ch.hearc.heg.scl.business.Meteo;
import ch.hearc.heg.scl.business.Pays;
import ch.hearc.heg.scl.business.ResultSearch;
import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.hibernate.utils.SessionConfiguration;
import ch.hearc.heg.scl.mapper.MeteoMapper;
import ch.hearc.heg.scl.mapper.PaysMapper;
import ch.hearc.heg.scl.mapper.StationMeteoMapper;
import ch.hearc.heg.scl.rmiObj.IOpenWeatherServices;
import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppService implements IOpenWeatherServices {
    public AppService() throws RemoteException {}

    /**
     * Méthode centrale, s'occupe de séparer le dto d'OpenWeatherMap, de persister dans la base, et de retourner le ResultSearch qu'on va envoyer au client
     */
    public ResultSearch fetchAndPersistWeather(OpenWeatherResponse dto) {
        // --- Mapping DTO → Entities ---
        PaysMapper paysMapper = new PaysMapper(dto, new CountryApi());
        MeteoMapper meteoMapper = new MeteoMapper(dto);
        StationMeteoMapper stationMapper = new StationMeteoMapper(dto, null, null);

        Pays pays = paysMapper.mapToEntity();
        StationMeteo station = stationMapper.mapToEntity();
        Meteo meteo = meteoMapper.mapToEntity();
        ResultSearch rs = null;

        try (Session session = SessionConfiguration.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // --- Pays ---
                Pays existingPays = findPaysByCode(session, pays.getCode());
                if (existingPays == null) {
                    paysMapper.insert(session, pays);
                    session.flush();
                } else {
                    pays = existingPays;
                }

                // --- Station ---
                StationMeteo existingStation = findStationByNameAndPays(session, station.getNom(), pays);
                if (existingStation == null) {
                    station.setPays(pays);
                    stationMapper.insert(session, station);
                    session.flush();
                } else {
                    station = existingStation;
                }

                // --- Meteo ---
                if (station.getNumero() == null) {
                    throw new IllegalStateException("Station non persistée correctement !");
                }

                // Vérifier si la mesure existe déjà
                Meteo existingMeteo = findMeteoByStationAndDate(session, station.getNumero(), meteo.getDateMesure());

                if (existingMeteo == null) {
                    meteo.setStation(station);
                    session.persist(meteo);
                    session.flush();
                } else {
                    System.out.println("La mesure pour cette station et cette date existe déjà, insertion ignorée.");
                    meteo = existingMeteo; // optionnel : utiliser la mesure existante
                }

                // Commit transaction
                tx.commit();

                // --- Détacher les collections pour RMI et ajouter les objets dans le ResultSearch---
                rs = new ResultSearch(
                        detachPays(pays),
                        detachMeteo(meteo),
                        detachStation(station)
                );

            } catch (Exception e) {
                if (tx != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de l'insertion des données météo", e);
            }
        }

        return rs;
    }

    /**
     * Recherche si une météo a déjà été créé par rapport à notre dernière requête API
     */
    private Meteo findMeteoByStationAndDate(Session session, Integer stationId, LocalDateTime dateMesure) {
        return session.createQuery(
                        "from Meteo m where m.station.numero = :stationId and m.dateMesure = :dateMesure", Meteo.class)
                .setParameter("stationId", stationId)
                .setParameter("dateMesure", dateMesure)
                .uniqueResultOptional()
                .orElse(null);
    }

    /**
     * Détache le lien entre Hibernate et les données d'un pays au lieu de laisser Hibernate gérer, afin de pouvoir l'envoyer au client
     */
    private Pays detachPays(Pays p) {
        Pays copy = new Pays();
        copy.setCode(p.getCode());
        copy.setNom(p.getNom());
        return copy;
    }
    /**
     * Détache le lien entre Hibernate et les données d'une mesure Météo au lieu de laisser Hibernate gérer, afin de pouvoir l'envoyer au client
     */
    private Meteo detachMeteo(Meteo m) {
        if (m == null) return null;

        Meteo copy = new Meteo();
        copy.setNumero(m.getNumero());
        copy.setDateMesure(m.getDateMesure());
        copy.setTemperature(m.getTemperature());
        copy.setPression(m.getPression());
        copy.setHumidite(m.getHumidite());
        copy.setVisibilite(m.getVisibilite());
        copy.setPrecipitation(m.getPrecipitation());

        if (m.getTexte() != null) {
            copy.setTexte(new ArrayList<>(m.getTexte())); // NO PersistentBag
        }

        copy.setStation(null); // 🚨 IMPORTANT : on coupe la boucle et on évite les proxys

        return copy;
    }

    /**
     * Détache le lien entre Hibernate et les données d'une station météo au lieu de laisser Hibernate gérer, afin de pouvoir l'envoyer au client
     */
    private StationMeteo detachStation(StationMeteo s) {

        StationMeteo copy = new StationMeteo();
        copy.setNumero(s.getNumero());
        copy.setNom(s.getNom());
        copy.setLatitude(s.getLatitude());
        copy.setLongitude(s.getLongitude());
        copy.setOpenWeatherMapId(s.getOpenWeatherMapId());

        // copie propre de la liste de Meteo
        List<Meteo> newList = new ArrayList<>();
        for (Meteo m : s.getDonneesMeteo()) {

            if (m == null) continue;

            Meteo m2 = new Meteo();
            m2.setNumero(m.getNumero());
            m2.setDateMesure(m.getDateMesure());
            m2.setTemperature(m.getTemperature());

            // copie du texte (plus de PersistentBag)
            if (m.getTexte() != null)
                m2.setTexte(new ArrayList<>(m.getTexte()));

            newList.add(m2);
        }

        copy.setDonneesMeteo(newList);

        return copy;
    }

    /**
     * Vérifie les coordonnées, et renvoie soit un null si ca n'existe pas, soit le dto
     */
    public OpenWeatherResponse VerifyLocation(double lat, double lon){
        try{
        OpenWeatherApi api = new OpenWeatherApi();
        String json = api.callApi(lat, lon);
        Gson gson = new Gson();
        return gson.fromJson(json, OpenWeatherResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifie les coordonnées, et renvoie soit un null si ca n'existe pas, soit le dto
     */
    private Pays findPaysByCode(Session session, String code) {
        Query<Pays> query = session.createQuery("from Pays p where p.code = :code", Pays.class);
        query.setParameter("code", code);
        return query.uniqueResult();
    }

    /**
     * Retourne la station météo par son nom et le pays de la station (évite les NewCastle)
     */
    private StationMeteo findStationByNameAndPays(Session session, String nom, Pays pays) {
        Query<StationMeteo> query = session.createQuery(
                "from StationMeteo s where s.nom = :nom and s.pays.code = :code",
                StationMeteo.class
        );
        query.setParameter("nom", nom);
        query.setParameter("code", pays.getCode());
        return query.uniqueResult();
    }

    @Override
    public ResultSearch getWeatherByCoordinates(double lat, double lon) throws RemoteException {

        System.out.println("Appel getWeatherByCoordinates côté serveur : " + lat + "," + lon);

        try {
            OpenWeatherResponse orw = VerifyLocation(lat, lon);
            if (orw != null) {
                return fetchAndPersistWeather(orw);
            } else {
                System.out.println("Le client a entré des coordonnées inexistantes : " + lat + ", " + lon);
                return null;
            }

        } catch (Exception e) {

            // LOG SERVEUR
            System.err.println("🔥 ERREUR SERVEUR getWeatherByCoordinates : " + e.getClass().getName());
            System.err.println("➡ Message : " + e.getMessage());
            e.printStackTrace();

            throw new RemoteException("Erreur interne du serveur lors de la récupération de la météo.");
        }
    }


    @Override
    public void refreshAllStations() throws RemoteException {
    //a faire
        System.out.println("Appel getWeatherByCoordinates côté serveur pour toutes les stations : ");
        ArrayList<StationMeteo> listeStation = getStations();
        for (StationMeteo s : listeStation) {
            try {
                System.out.println("Raffraichissement de la station : " +  s.getNom());
                OpenWeatherApi api = new OpenWeatherApi();
                String json = api.callApi(s.getOpenWeatherMapId());
                Gson gson = new Gson();
                OpenWeatherResponse orw = gson.fromJson(json, OpenWeatherResponse.class);
                if (orw != null) {
                    fetchAndPersistWeather(orw);
                } else {
                    System.out.println("Le client a entré une station inexistante (ce n'est pas senscé arriver)");
                }

            } catch (Exception e) {

                // LOG SERVEUR
                System.err.println("🔥 ERREUR SERVEUR getWeatherByCoordinates : " + e.getClass().getName());
                System.err.println("➡ Message : " + e.getMessage());
                e.printStackTrace();

                throw new RemoteException("Erreur interne du serveur lors de la récupération de la météo.");
            }
        }
    }

    @Override
    public ArrayList<StationMeteo> getStations() {
        try (Session session = SessionConfiguration.getSessionFactory().openSession()) {
            //HQL
            List<StationMeteo> stations = session
                    .createQuery("from StationMeteo", StationMeteo.class)
                    .list();

            // 🔥 Détache les collections Hibernate pour éviter ces fichus PersistentBag
            for (StationMeteo s : stations) {
                s.setDonneesMeteo(new ArrayList<>());
            }

            return new ArrayList<>(stations);

        }
    }

    @Override
    public ResultSearch getWeatherForStation(int stationId) throws RemoteException {
        System.out.println("Appel getWeatherForStation côté serveur pour station : " + stationId);

        try {
            // --- Appel API externe ---
            OpenWeatherApi api = new OpenWeatherApi();
            String json = api.callApi(stationId);
            Gson gson = new Gson();
            OpenWeatherResponse orw = gson.fromJson(json, OpenWeatherResponse.class);

            if (orw == null) {
                System.out.println("Le client a entré une station inexistante (ce n'est pas censé arriver)");
                return null;
            }
            // --- Persist et map DTO -> Entities ---
            ResultSearch rs = fetchAndPersistWeather(orw);
            // --- Charger toutes les mesures météo depuis la base ---
            detachStationData(rs.getStationMeteo());
            // --- Détacher Pays et Meteo pour sécurité ---
            rs = detachResultSearch(rs);

            return rs;

        } catch (Exception e) {
            System.err.println("🔥 ERREUR SERVEUR getWeatherForStation : " + e.getClass().getName());
            System.err.println("➡ Message : " + e.getMessage());
            e.printStackTrace();

            throw new RemoteException("Erreur interne du serveur lors de la récupération de la météo.");
        }
    }

    /**
     * Charge toutes les données météo depuis la DB pour la station et détache les collections Hibernate
     */
    private void detachStationData(StationMeteo stationMeteo) {
        if (stationMeteo == null) return;

        try (Session session = SessionConfiguration.getSessionFactory().openSession()) {
            List<Meteo> mesures = session.createQuery(
                            "from Meteo m where m.station.numero = :stationId order by m.dateMesure asc", Meteo.class)
                    .setParameter("stationId", stationMeteo.getNumero())
                    .getResultList();

            // Convertir toutes les collections en ArrayList pour éviter PersistentBag
            List<Meteo> copy = new ArrayList<>();
            for (Meteo m : mesures) {
                if (m.getTexte() != null) {
                    m.setTexte(new ArrayList<>(m.getTexte()));
                }
                copy.add(m);
            }

            stationMeteo.setDonneesMeteo(copy);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données météo : " + e.getMessage());
        }
    }

    /**
     * Détache complètement un ResultSearch pour le rendre sérialisable
     */
    private ResultSearch detachResultSearch(ResultSearch rs) {
        if (rs == null) return null;

        // Détacher Pays
        Pays pays = null;
        if (rs.getPays() != null) {
            Pays p = rs.getPays();
            pays = new Pays();
            pays.setNumero(p.getNumero());
            pays.setCode(p.getCode());
            pays.setNom(p.getNom());
        }

        // Détacher Meteo
        Meteo meteo = null;
        if (rs.getMeteo() != null) {
            Meteo m = rs.getMeteo();
            meteo = new Meteo();
            meteo.setNumero(m.getNumero());
            meteo.setStation(null); // on a déjà la station complète
            meteo.setDateMesure(m.getDateMesure());
            meteo.setTemperature(m.getTemperature());
            meteo.setPression(m.getPression());
            meteo.setHumidite(m.getHumidite());
            meteo.setVisibilite(m.getVisibilite());
            meteo.setPrecipitation(m.getPrecipitation());
            meteo.setTexte(m.getTexte() != null ? new ArrayList<>(m.getTexte()) : new ArrayList<>());
        }

        // Détacher StationMeteo
        StationMeteo station = null;
        if (rs.getStationMeteo() != null) {
            StationMeteo s = rs.getStationMeteo();
            station = new StationMeteo();
            station.setNumero(s.getNumero());
            station.setNom(s.getNom());
            station.setPays(pays);
            station.setLatitude(s.getLatitude());
            station.setLongitude(s.getLongitude());
            station.setOpenWeatherMapId(s.getOpenWeatherMapId());

            List<Meteo> copy = new ArrayList<>();
            if (s.getDonneesMeteo() != null) {
                for (Meteo m : s.getDonneesMeteo()) {
                    Meteo mm = new Meteo();
                    mm.setNumero(m.getNumero());
                    mm.setStation(null); // pour éviter boucle
                    mm.setDateMesure(m.getDateMesure());
                    mm.setTemperature(m.getTemperature());
                    mm.setPression(m.getPression());
                    mm.setHumidite(m.getHumidite());
                    mm.setVisibilite(m.getVisibilite());
                    mm.setPrecipitation(m.getPrecipitation());
                    mm.setTexte(m.getTexte() != null ? new ArrayList<>(m.getTexte()) : new ArrayList<>());
                    copy.add(mm);
                }
            }
            station.setDonneesMeteo(copy);
        }

        return new ResultSearch(pays, meteo, station);
    }

}
