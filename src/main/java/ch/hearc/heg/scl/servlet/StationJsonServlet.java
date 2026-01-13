package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.services.AppService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.rmi.RemoteException;

@WebServlet("/station-json")
public class StationJsonServlet extends HttpServlet {
    private final AppService appService = new AppService();

    public StationJsonServlet() throws RemoteException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String numero = request.getParameter("numero");
        StationMeteo station = appService.getStationByNumero(Integer.parseInt(numero));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (station != null) {
            response.getWriter().write(new Gson().toJson(station));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Station non trouvée\"}");
        }
    }
}

