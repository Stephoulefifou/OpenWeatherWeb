package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.services.AppService;
import com.google.gson.Gson;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int numero = Integer.parseInt(req.getParameter("numero"));
        StationMeteo station = appService.getStationByNumero(numero);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        new Gson().toJson(station, resp.getWriter());
    }
}