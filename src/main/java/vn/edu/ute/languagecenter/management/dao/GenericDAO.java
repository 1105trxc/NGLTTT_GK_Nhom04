package vn.edu.ute.languagecenter.management.dao;

import jakarta.persistence.EntityManager;
import vn.edu.ute.languagecenter.management.db.Jpa;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO cung cấp các thao tác CRUD cơ bản cho mọi Entity.
 * Các DAO con chỉ cần extends và bổ sung truy vấn đặc thù.
 */
public abstract class GenericDAO<T> {

    private final Class<T> entityClass;

    protected GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ---- helpers ----
    protected EntityManager em() {
        return Jpa.em();
    }

    // ---- CRUD ----

    public T save(T entity) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public T update(T entity) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            T merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<T> findById(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(entityClass, id));
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
