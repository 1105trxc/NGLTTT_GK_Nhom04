package vn.edu.ute.languagecenter.management.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.db.Jpa;

import java.util.List;
import java.util.Optional;

/**
 * GenericDAO<T> - Lớp DAO dùng chung cho tất cả Entity.
 * Cung cấp các thao tác CRUD cơ bản: tìm kiếm, lưu, cập nhật, xóa.
 *
 * @param <T> Kiểu Entity (ví dụ: Teacher, Student, ...)
 */
public abstract class GenericDAO<T> {

    // Kiểu dữ liệu của Entity mà DAO này quản lý
    private final Class<T> entityClass;

    protected GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Tạo EntityManager mới từ Jpa factory.
     * Mỗi giao dịch nên dùng một EntityManager riêng biệt.
     */
    protected EntityManager getEntityManager() {
        return Jpa.em();
    }

    /**
     * Lưu một Entity mới vào database.
     * 
     * @param entity đối tượng cần lưu
     */
    public void save(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity); // persist: đưa entity vào trạng thái managed và INSERT vào DB
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback(); // rollback nếu có lỗi
            throw new RuntimeException("Lỗi khi lưu entity: " + e.getMessage(), e);
        } finally {
            em.close(); // luôn đóng EM sau khi dùng xong
        }
    }

    /**
     * Cập nhật một Entity đã tồn tại trong database.
     * 
     * @param entity đối tượng cần cập nhật (phải có ID)
     * @return entity đã được merge
     */
    public T update(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity); // merge: cập nhật entity nếu đã tồn tại
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

    /**
     * Xóa một Entity khỏi database theo ID.
     * 
     * @param id khóa chính của entity cần xóa
     */
    public void delete(Object id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id); // tìm entity theo ID trước
            if (entity != null) {
                em.remove(entity); // xóa entity khỏi DB
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi xóa entity: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Tìm một Entity theo ID (khóa chính).
     * 
     * @param id khóa chính
     * @return Optional chứa entity nếu tìm thấy
     */
    public Optional<T> findById(Object id) {
        EntityManager em = getEntityManager();
        try {
            T entity = em.find(entityClass, id); // em.find trả về null nếu không tìm thấy
            return Optional.ofNullable(entity);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy toàn bộ danh sách Entity trong bảng.
     * 
     * @return List<T> danh sách tất cả các bản ghi
     */
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            // Dùng JPQL (HQL): "SELECT t FROM <ClassName> t" để truy vấn tất cả
            String jpql = "SELECT t FROM " + entityClass.getSimpleName() + " t";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
