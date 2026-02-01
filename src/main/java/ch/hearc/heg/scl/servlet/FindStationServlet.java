package ch.hearc.heg.scl.servlet;

import ch.hearc.heg.scl.business.ResultSearch;
import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.services.AppService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.rmi.RemoteException;

@WebServlet("/findStation")
public class FindStationServlet extends HttpServlet {
    private AppService appService = new AppService();

    public FindStationServlet() throws RemoteException {
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Affiche juste le formulaire si pas de params
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String latStr = request.getParameter("latitude");
        String lonStr = request.getParameter("longitude");

        if (latStr == null || lonStr == null) {
            request.setAttribute("error", "Merci de remplir les deux champs.");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        try {
            double latitude = Double.parseDouble(latStr);
            double longitude = Double.parseDouble(lonStr);

            ResultSearch result = appService.getWeatherByCoordinates(latitude, longitude);

            if (result == null || result.getStationMeteo() == null) {
                request.setAttribute("error", "🤷‍♂️ Aucune station trouvée à proximité.");
            } else {
                // 🔥 SOLUTION ICI : On recharge la station avec son numéro pour avoir sa LISTE DE METEO
                StationMeteo stationComplete = appService.getStationByNumero(result.getStationMeteo().getNumero());
                result.setStationMeteo(stationComplete);

                request.setAttribute("station", result);
            }
        }
        catch (java.rmi.RemoteException e) {
            // 🔥 CAS NORMAL : API ne trouve rien
            request.setAttribute(
                    "error",
                    "🌊❄️ Aucune station météo trouvée à proximité (zone isolée ou océan)."
            );
        }
        catch (NumberFormatException e) {
            request.setAttribute(
                    "error",
                    "Latitude ou longitude invalide."
            );
        }
        catch (Exception e) {
            // 🚨 VRAIE ERREUR
            e.printStackTrace(); // important pour debug
            request.setAttribute(
                    "error",
                    "🚨 Erreur technique lors de l’appel à l’API météo."
            );
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }


}
