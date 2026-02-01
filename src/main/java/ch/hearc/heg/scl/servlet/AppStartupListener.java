package ch.hearc.heg.scl.servlet;
import ch.hearc.heg.scl.services.AppService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Démarrage du site - rafraîchissement des stations");

    new Thread(()  -> {

    try{
        AppService appService = new AppService();
        appService.refreshAllStations();
        System.out.println("Rafraîchissement des stations terminé avec succès.");

    } catch (Exception e) {
        System.err.println("Erreur lors du rafraîchissement des stations au démarrage : " + e.getMessage());
    }
    }).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application arrêtée !");
    }
}
