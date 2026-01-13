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

@WebServlet("/station")
public class StationDetailServlet extends HttpServlet {

    private final AppService appService = new AppService();

    public StationDetailServlet() throws RemoteException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String numeroParam = request.getParameter("numero");

        if (numeroParam == null) {
            response.sendRedirect(request.getContextPath() + "/stations");
            return;
        }

        int numeroStation;
        try {
            numeroStation = Integer.parseInt(numeroParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/stations");
            return;
        }

        System.out.println("➡ StationDetailServlet appelé avec numero = " + numeroStation);

        // Récupère la station depuis AppService
        StationMeteo station = null;
        try {
            station = appService.getStationByNumero(numeroStation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (station == null) {
            response.sendRedirect(request.getContextPath() + "/stations");
            return;
        }

        request.setAttribute("station", station);

        // Forward vers la JSP de détail
        request.getRequestDispatcher("/station.jsp").forward(request, response);
    }

}
