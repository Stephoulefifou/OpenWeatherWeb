package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.persistance.StationMeteoDAO;
import ch.hearc.heg.scl.services.AppService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

@WebServlet("/map")
public class MapServlet extends HttpServlet {
    private AppService appService = new AppService();

    public MapServlet() throws RemoteException {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<StationMeteo> stationsMeteo = appService.getStations();
        req.setAttribute("stations", stationsMeteo);
        req.getRequestDispatcher("/map.jsp").forward(req, resp);
    }
}
