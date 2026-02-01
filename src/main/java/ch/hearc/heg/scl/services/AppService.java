package ch.hearc.heg.scl.services;

import ch.hearc.heg.scl.business.Meteo;
import ch.hearc.heg.scl.business.Pays;
import ch.hearc.heg.scl.business.ResultSearch;
import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.hibernate.utils.SessionConfiguration;
import ch.hearc.heg.scl.mapper.MeteoMapper;
import ch.hearc.heg.scl.mapper.PaysMapper;
import ch.hearc.heg.scl.mapper.StationMeteoMapper;
import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppService  {
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

        // --- Températures ---
        copy.setTemperature(m.getTemperature());
        copy.setRessenti(m.getRessenti());
        copy.setTempMin(m.getTempMin());
        copy.setTempMax(m.getTempMax());

        // --- Atmosphère ---
        copy.setPression(m.getPression());
        copy.setHumidite(m.getHumidite());
        copy.setVisibilite(m.getVisibilite());
        copy.setPrecipitation(m.getPrecipitation());

        // --- Vent ---
        copy.setVentVitesse(m.getVentVitesse());
        copy.setVentDirection(m.getVentDirection());
        copy.setVentRafales(m.getVentRafales());

        // --- Éphéméride ---
        copy.setLeverSoleil(m.getLeverSoleil());
        copy.setCoucherSoleil(m.getCoucherSoleil());

        // --- Descriptions (Détachage de la liste Hibernate) ---
        if (m.getTexte() != null) {
            copy.setTexte(new ArrayList<>(m.getTexte()));
        }

        copy.setStation(null); // Coupe la boucle récursive pour la sérialisation

        return copy;
    }

    /**
     * Détache le lien entre Hibernate et les données d'une station météo au lieu de laisser Hibernate gérer, afin de pouvoir l'envoyer au client
     */
    private StationMeteo detachStation(StationMeteo s) {
        if (s == null) return null; // Sécurité

        StationMeteo copy = new StationMeteo();
        copy.setNumero(s.getNumero());
        copy.setNom(s.getNom());
        copy.setLatitude(s.getLatitude());
        copy.setLongitude(s.getLongitude());
        copy.setOpenWeatherMapId(s.getOpenWeatherMapId());

        List<Meteo> newList = new ArrayList<>();
        if (s.getDonneesMeteo() != null) {
            for (Meteo m : s.getDonneesMeteo()) {
                // Utilise la méthode detachMeteo que tu as déjà corrigée !
                // Ça évite de réécrire 15 lignes et d'oublier des champs.
                newList.add(detachMeteo(m));
            }
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

    public ResultSearch getWeatherForStation(int databaseId) throws RemoteException {
        System.out.println("Appel getWeatherForStation pour ID base de données : " + databaseId);

        try (Session session = SessionConfiguration.getSessionFactory().openSession()) {
            // 1. On va chercher la station en base pour trouver son VRAI ID OpenWeatherMap
            StationMeteo station = session.get(StationMeteo.class, databaseId);

            if (station == null || station.getOpenWeatherMapId() == null) {
                System.err.println("Station introuvable ou pas d'ID OpenWeatherMap pour : " + databaseId);
                return null;
            }

            int owmId = station.getOpenWeatherMapId();
            System.out.println("VRAI ID OpenWeatherMap trouvé : " + owmId);

            // 2. On appelle l'API avec le BON identifiant
            OpenWeatherApi api = new OpenWeatherApi();
            String json = api.callApi(owmId); // <-- C'est l'ID OWM ici !

            if (json == null || json.contains("404") || json.contains("error")) {
                System.err.println("L'API OpenWeather a renvoyé une erreur pour l'ID " + owmId + " : " + json);
                return null;
            }

            Gson gson = new Gson();
            OpenWeatherResponse orw = gson.fromJson(json, OpenWeatherResponse.class);

            // 3. On persiste les nouvelles données (ça créera une nouvelle ligne dans METEO)
            ResultSearch rs = fetchAndPersistWeather(orw);

            // 4. On recharge tout proprement pour le retour
            detachStationData(rs.getStationMeteo());
            return detachResultSearch(rs);

        } catch (Exception e) {
            System.err.println("🔥 ERREUR SERVEUR : " + e.getMessage());
            throw new RemoteException("Erreur lors du rafraîchissement.");
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

            List<Meteo> copy = new ArrayList<>();
            for (Meteo m : mesures) {
                // ENCORE UNE FOIS : Utilise detachMeteo(m) ici !
                copy.add(detachMeteo(m));
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

        // 1. Détacher le Pays
        Pays pays = null;
        if (rs.getPays() != null) {
            Pays p = rs.getPays();
            pays = new Pays();
            pays.setNumero(p.getNumero());
            pays.setCode(p.getCode());
            pays.setNom(p.getNom());
        }

        // 2. Détacher la mesure Météo principale (en utilisant notre méthode du dessus)
        Meteo meteo = detachMeteo(rs.getMeteo());

        // 3. Détacher la StationMeteo et sa liste de mesures
        StationMeteo station = null;
        if (rs.getStationMeteo() != null) {
            StationMeteo s = rs.getStationMeteo();
            station = new StationMeteo();
            station.setNumero(s.getNumero());
            station.setNom(s.getNom());
            station.setPays(pays); // On utilise le pays déjà détaché
            station.setLatitude(s.getLatitude());
            station.setLongitude(s.getLongitude());
            station.setOpenWeatherMapId(s.getOpenWeatherMapId());

            // Copie propre de la liste des mesures de la station
            List<Meteo> copyListe = new ArrayList<>();
            if (s.getDonneesMeteo() != null) {
                for (Meteo m : s.getDonneesMeteo()) {
                    // On réutilise detachMeteo pour chaque élément de la liste
                    copyListe.add(detachMeteo(m));
                }
            }
            station.setDonneesMeteo(copyListe);
        }

        return new ResultSearch(pays, meteo, station);
    }


    /**
     * Retourne la station complète avec toutes ses mesures météo détachées, par son numero.
     */
    public StationMeteo getStationByNumero(int numero) {
        try (Session session = SessionConfiguration.getSessionFactory().openSession()) {
            StationMeteo station = session.createQuery(
                            "from StationMeteo s where s.numero = :numero", StationMeteo.class)
                    .setParameter("numero", numero)
                    .uniqueResult();

            if (station == null) return null;

            List<Meteo> mesures = session.createQuery(
                            "from Meteo m where m.station.numero = :stationId order by m.dateMesure asc", Meteo.class)
                    .setParameter("stationId", numero)
                    .getResultList();

            List<Meteo> copy = new ArrayList<>();
            for (Meteo m : mesures) {
                if (m == null) continue;
                // Appelle simplement ta méthode utilitaire :
                copy.add(detachMeteo(m));
            }
            station.setDonneesMeteo(copy);

            if (station.getPays() != null) {
                Pays p = station.getPays();
                Pays copyPays = new Pays();
                copyPays.setNumero(p.getNumero());
                copyPays.setNom(p.getNom());
                copyPays.setCode(p.getCode());
                station.setPays(copyPays);
            }

            return station;
        }
    }
    public ResultSearch refreshStation(int stationId) throws RemoteException {
        return getWeatherForStation(stationId);
    }

}
