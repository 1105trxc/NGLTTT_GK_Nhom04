package vn.edu.ute.languagecenter.management.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Branch;

import java.util.List;

/**
 * BranchDAO - DAO chuyên biệt cho entity Branch (Chi nhánh).
 * Kế thừa GenericDAO, bổ sung truy vấn đặc thù.
 */
public class BranchDAO extends GenericDAO<Branch> {

    public BranchDAO() {
        super(Branch.class);
    }

    /**
     * Lấy tất cả chi nhánh đang hoạt động, sắp xếp theo tên A-Z.
     * 
     * @return danh sách Branch Active
     */
    public List<Branch> findAllActive() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT b FROM Branch b WHERE b.status = 'Active' ORDER BY b.branchName ASC";
            TypedQuery<Branch> q = em.createQuery(hql, Branch.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm chi nhánh theo tên.
     * 
     * @param name tên chi nhánh (tìm kiếm gần đúng)
     * @return danh sách chi nhánh khớp tên
     */
    public List<Branch> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT b FROM Branch b WHERE LOWER(b.branchName) LIKE LOWER(:name)";
            TypedQuery<Branch> q = em.createQuery(hql, Branch.class);
            q.setParameter("name", "%" + name + "%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
