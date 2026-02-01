package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.business.Meteo;
import ch.hearc.heg.scl.services.AppService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.w3c.dom.html.HTMLQuoteElement;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/station-json")
public class StationJsonServlet extends HttpServlet {
    private final AppService appService;

    public StationJsonServlet() throws RemoteException {
        this.appService = new AppService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String numeroParam = request.getParameter("numero");
            if (numeroParam == null || numeroParam.isEmpty()) {
                response.setStatus(400);
                response.getWriter().write("{\"error\":\"Numéro manquant\"}");
                return;
            }

            int numero = Integer.parseInt(numeroParam);
            StationMeteo station = appService.getStationByNumero(numero);

            if (station == null) {
                response.setStatus(404);
                response.getWriter().write("{\"error\":\"Station introuvable\"}");
                return;
            }

            // Préparation manuelle du JSON pour être SÛR des noms de champs
            Map<String, Object> json = new java.util.HashMap<>();
            json.put("numero", station.getNumero());
            json.put("nom", station.getNom());
            json.put("latitude", station.getLatitude());
            json.put("longitude", station.getLongitude());

            Map<String, String> paysMap = new java.util.HashMap<>();
            paysMap.put("nom", station.getPays() != null ? station.getPays().getNom() : "Inconnu");
            json.put("pays", paysMap);

            List<Map<String, Object>> listeMeteo = new ArrayList<>();
            for (Meteo m : station.getDonneesMeteo()) {
                // IMPORTANT : Il faut remplir la map 'mm' sinon le JSON est vide !
                Map<String, Object> mm = new java.util.HashMap<>();

                mm.put("date", m.getPrettyDate());
                mm.put("temp", m.getTemperature());
                mm.put("ressenti", m.getRessenti());
                mm.put("tempMin", m.getTempMin());
                mm.put("tempMax", m.getTempMax());
                mm.put("humi", m.getHumidite());
                mm.put("pression", m.getPression());
                mm.put("visibilite", m.getVisibilite());
                mm.put("precipitation", m.getPrecipitation());
                mm.put("ventVitesse", m.getVentVitesse());
                mm.put("ventDirection", m.getVentDirection());
                mm.put("ventRafales", m.getVentRafales());

                mm.put("leverSoleil", m.getLeverSoleil() != null ?
                        m.getLeverSoleil().format(DateTimeFormatter.ofPattern("HH:mm")) : "Inconnu");
                mm.put("coucherSoleil", m.getCoucherSoleil() != null ?
                        m.getCoucherSoleil().format(DateTimeFormatter.ofPattern("HH:mm")) : "Inconnu");

                mm.put("texte", m.getTexte()); // Liste des descriptions

                listeMeteo.add(mm); // On ajoute la map REMPLIE à la liste
            }

            json.put("donneesMeteo", listeMeteo);

            com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .create();

            response.getWriter().write(gson.toJson(json));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Erreur serveur\"}");
        }
    }


}


