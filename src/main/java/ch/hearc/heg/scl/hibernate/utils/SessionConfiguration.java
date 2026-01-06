package ch.hearc.heg.scl.hibernate.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class SessionConfiguration {

    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
        try {
            // 1️⃣ Crée le registre de services à partir du fichier XML
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml") // fichier XML dans src/main/resources
                    .build();

            System.out.println("Hibernate configuration loaded");

            // 2️⃣ Crée les métadonnées à partir du registre
            Metadata metadata = new MetadataSources(registry)
                    .getMetadataBuilder()
                    .build();

            // 3️⃣ Crée la SessionFactory
            sessionFactory = metadata.buildSessionFactory();

            System.out.println("SessionFactory created");

            return sessionFactory;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Initial SessionFactory creation failed: " + e);
        }
    }

    // Singleton getter
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    // Alias si tu veux garder getSessionAnnotationFactory
    public static SessionFactory getSessionAnnotationFactory() {
        return getSessionFactory();
    }
}
