package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.ResultSearch;
import ch.hearc.heg.scl.services.AppService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.rmi.RemoteException;

@WebServlet("/refresh-station")
public class RefreshStationServlet extends HttpServlet {
    private final AppService appService = new AppService();

    public RefreshStationServlet() throws RemoteException {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String numeroParam = request.getParameter("numero");
            int numero = Integer.parseInt(numeroParam);

            // On appelle le service de rafraîchissement
            appService.refreshStation(numero);

            // On répond un succès en JSON
            response.getWriter().write("{\"status\":\"ok\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Erreur lors du rafraîchissement\"}");
        }
    }
}

