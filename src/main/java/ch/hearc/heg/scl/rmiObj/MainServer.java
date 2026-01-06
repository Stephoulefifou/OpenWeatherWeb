package ch.hearc.heg.scl.rmiObj;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import ch.hearc.heg.scl.services.AppService;

public class MainServer {
    public static void main(String[] args) {
        try {
            // Forcer localhost
            System.setProperty("java.rmi.server.hostname", "localhost");
            // Faire sur plusieurs machine --> rôle Serveur :
            // System.setProperty("java.rmi.server.hostname", "IP DU SERVEUR");
            // Warm-up Hibernate

            System.out.println("Hibernate initialisé");

            // Créer le service
            AppService service = new AppService();

            // Exporter le service sur le port 2099 (objet RMI)
            IOpenWeatherServices stub =
                    (IOpenWeatherServices) UnicastRemoteObject.exportObject(service, 2099);

            // Créer le registry sur 1099
            Registry reg = LocateRegistry.createRegistry(1199);

            // Binder le service
            reg.rebind("HelloService", stub);

            System.out.println("Serveur RMI prêt !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
