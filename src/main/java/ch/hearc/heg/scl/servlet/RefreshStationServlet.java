package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.ResultSearch;
import ch.hearc.heg.scl.services.AppService;
import com.google.gson.Gson;
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int numero = Integer.parseInt(req.getParameter("numero"));
        ResultSearch rs = appService.getWeatherForStation(numero);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        new Gson().toJson(rs.getStationMeteo(), resp.getWriter());
    }
}

