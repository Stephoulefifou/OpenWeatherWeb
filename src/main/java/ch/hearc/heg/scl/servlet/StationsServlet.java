package ch.hearc.heg.scl.servlet;

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
}
