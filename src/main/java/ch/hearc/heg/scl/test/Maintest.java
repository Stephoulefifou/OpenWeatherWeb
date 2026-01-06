package ch.hearc.heg.scl.test;

import ch.hearc.heg.scl.business.StationMeteo;
import ch.hearc.heg.scl.rmiObj.IOpenWeatherServices;
import ch.hearc.heg.scl.services.AppService;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Maintest {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        try {
            // Création du service
            AppService service = new AppService();

            // Affiche les stations
            ShowStation(service);

        } catch (RemoteException e) {
            System.err.println("Erreur avec le service : " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static ArrayList<StationMeteo> ShowStation(AppService service) throws RemoteException {
        System.out.println("Stations météo :");
        ArrayList<StationMeteo> listeStation = service.getStations();
        int i = 0;

        for (StationMeteo station : listeStation) {
            System.out.println("-------------------------------------------");
            System.out.println((i + 1) + " : " + station.getNom()
                    + " | Lat : " + station.getLatitude()
                    + " | Lon : " + station.getLongitude()
                    + " | OpenWeatherId : " + station.getOpenWeatherMapId());
            i++;
        }

        return listeStation;
    }
}
