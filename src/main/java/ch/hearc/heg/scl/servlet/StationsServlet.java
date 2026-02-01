package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.Meteo;
import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.services.AppService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/stations")
public class StationsServlet extends HttpServlet {

    private final AppService appService = new AppService();;

    public StationsServlet() throws RemoteException {
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            System.out.println("AppService initialisé avec succès.");
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation AppService", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ArrayList<StationMeteo> stations = new ArrayList<>();

        try {
            stations = appService.getStations();
            for (StationMeteo s : stations) {
                if (s.getNumero() != null) {
                    List<Meteo> mesures = appService.getStationByNumero(s.getNumero()).getDonneesMeteo();
                    if (mesures != null && !mesures.isEmpty()) {
                        // La dernière mesure = la plus récente (ordre croissant dans getStationByNumero)
                        s.setDonneesMeteo(List.of(mesures.get(mesures.size() - 1)));
                    }
                }
            }
            System.out.println("Stations récupérées : " + (stations != null ? stations.size() : "null"));

            for (StationMeteo s : stations) {
                System.out.println(s.getNom() + " | " + s.getLatitude() + " | " + s.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Passe la liste à la JSP
        request.setAttribute("stations", stations);
        // Forward vers la JSP
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("refreshAll".equals(action)) {
                System.out.println("🔄 Rafraîchissement de toutes les stations demandé depuis l'IHM");
                appService.refreshAllStations();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Erreur lors du rafraîchissement des stations", e);
        }

        // Redirige vers le GET pour recharger la liste mise à jour
        response.sendRedirect(request.getContextPath() + "/stations");
    }

}






























