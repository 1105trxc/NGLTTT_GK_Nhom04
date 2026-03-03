package vn.edu.ute.languagecenter.management.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class Jpa {

    private static EntityManagerFactory emf;

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("LanguageCenterPU");

    private Jpa() {
    }

    public static EntityManager em() {
        if (emf == null) {
            try {
                emf = Persistence.createEntityManagerFactory("languageCenterPU");
            } catch (Exception e) {
                System.err.println("Cannot init EntityManagerFactory");
                e.printStackTrace();
                throw e;
            }
        }
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf != null) {
            emf.close();
        }
    }
}