package vn.edu.ute.languagecenter.management.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.db.Jpa;

import java.util.List;
import java.util.Optional;

/**
 * GenericRepository<T> — lớp JPA base dùng chung cho tất cả JpaXxxRepository.
 * Cung cấp CRUD cơ bản: save, update, delete, findById, findAll.
 *
 * @param <T> Kiểu Entity (ví dụ: Teacher, Student, ...)
 */
public abstract class GenericRepository<T> {

    private final Class<T> entityClass;

    protected GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        return Jpa.em();
    }

    /** INSERT entity mới vào DB. */
    public void save(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu entity: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /** UPDATE entity đã tồn tại trong DB. */
    public T update(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi cập nhật entity: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /** DELETE entity theo ID. */
    public void delete(Object id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null)
                em.remove(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi xóa entity: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /** Tìm entity theo khóa chính. */
    public Optional<T> findById(Object id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(entityClass, id));
        } finally {
            em.close();
        }
    }

    /** Lấy toàn bộ entity trong bảng. */
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT t FROM " + entityClass.getSimpleName() + " t";
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }
}
