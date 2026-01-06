package ch.hearc.heg.scl.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ch.hearc.heg.scl.hibernate.utils.SessionConfiguration;

@WebListener
public class HibernateInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialize Hibernate SessionFactory on startup
        SessionConfiguration.getSessionFactory();
        System.out.println("Hibernate initialized at startup");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup on shutdown
        SessionConfiguration.shutdown();
        System.out.println("Hibernate shutdown");
    }
}
