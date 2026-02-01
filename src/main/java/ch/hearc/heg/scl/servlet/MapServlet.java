package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.persistance.StationMeteoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/map")
public class MapServlet extends HttpServlet {
    private StationMeteoDAO stationMeteoDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<StationMeteo> stationsMeteo = stationMeteoDAO.findAll();
        req.setAttribute("stations", stationsMeteo);
        req.getRequestDispatcher("/WEB-INF/views/map.jsp").forward(req, resp);
    }
}
