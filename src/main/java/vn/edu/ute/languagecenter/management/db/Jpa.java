package vn.edu.ute.languagecenter.management.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class Jpa {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("LanguageCenterPU");

    private Jpa() {
    }

    public static EntityManager em() {
        return EMF.createEntityManager();
    }

    public static void shutdown() {
        EMF.close();
    }
}
