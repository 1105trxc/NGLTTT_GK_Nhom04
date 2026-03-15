package vn.edu.ute.languagecenter.management.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.db.Jpa;
import vn.edu.ute.languagecenter.management.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to fetch aggregate real-time statistics for the Statistics Page.
 */
public class DashboardStatsService {

    public long getTotalStudents(LocalDate from, LocalDate to) {
        EntityManager em = Jpa.em();
        try {
            String q = "SELECT COUNT(s) FROM Student s WHERE s.createdAt >= :from AND s.createdAt <= :to";
            TypedQuery<Long> query = em.createQuery(q, Long.class);
            query.setParameter("from", from.atStartOfDay());
            query.setParameter("to", to.atTime(LocalTime.MAX));
            Long count = query.getSingleResult();
            return count != null ? count : 0L;
        } catch (NoResultException e) {
            return 0L;
        } finally {
            em.close();
        }
    }

    public long getTotalClasses(LocalDate from, LocalDate to) {
        EntityManager em = Jpa.em();
        try {
            String q = "SELECT COUNT(c) FROM Class_ c WHERE c.createdAt >= :from AND c.createdAt <= :to";
            TypedQuery<Long> query = em.createQuery(q, Long.class);
            query.setParameter("from", from.atStartOfDay());
            query.setParameter("to", to.atTime(LocalTime.MAX));
            Long count = query.getSingleResult();
            return count != null ? count : 0L;
        } catch (NoResultException e) {
            return 0L;
        } finally {
            em.close();
        }
    }

    public BigDecimal getTotalRevenue(LocalDate from, LocalDate to) {
        EntityManager em = Jpa.em();
        try {
            // Use Object.class to be more flexible with what Hibernate returns for SUM
            String q = "SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = :status AND i.issueDate >= :from AND i.issueDate <= :to";
            Object sum = em.createQuery(q)
                    .setParameter("status", Invoice.InvoiceStatus.Paid)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
            
            if (sum == null) return BigDecimal.ZERO;
            if (sum instanceof BigDecimal) return (BigDecimal) sum;
            if (sum instanceof Number) return new BigDecimal(((Number) sum).toString());
            return BigDecimal.ZERO;
        } catch (NoResultException e) {
            return BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }

    public long getTotalResults(LocalDate from, LocalDate to) {
        EntityManager em = Jpa.em();
        try {
            String q = "SELECT COUNT(r) FROM Result r WHERE r.createdAt >= :from AND r.createdAt <= :to";
            TypedQuery<Long> query = em.createQuery(q, Long.class);
            query.setParameter("from", from.atStartOfDay());
            query.setParameter("to", to.atTime(LocalTime.MAX));
            Long count = query.getSingleResult();
            return count != null ? count : 0L;
        } catch (NoResultException e) {
            return 0L;
        } finally {
            em.close();
        }
    }

    /**
     * Get revenue grouped by day for the chart.
     */
    public Map<LocalDate, BigDecimal> getDailyRevenue(LocalDate from, LocalDate to) {
        EntityManager em = Jpa.em();
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        
        // Initialize map with 0 for all days in range
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            map.put(d, BigDecimal.ZERO);
        }

        try {
            String q = "SELECT i.issueDate, SUM(i.totalAmount) FROM Invoice i " +
                       "WHERE i.status = :status AND i.issueDate >= :from AND i.issueDate <= :to " +
                       "GROUP BY i.issueDate " +
                       "ORDER BY i.issueDate ASC";
            
            List<Object[]> results = em.createQuery(q, Object[].class)
                    .setParameter("status", Invoice.InvoiceStatus.Paid)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();

            for (Object[] row : results) {
                LocalDate date = (LocalDate) row[0];
                Object amtObj = row[1];
                BigDecimal amount = BigDecimal.ZERO;
                if (amtObj instanceof BigDecimal) amount = (BigDecimal) amtObj;
                else if (amtObj instanceof Number) amount = new BigDecimal(((Number) amtObj).toString());
                
                if (map.containsKey(date)) {
                    map.put(date, amount);
                }
            }
        } finally {
            em.close();
        }
        return map;
    }
    public long getTotalTeachers() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT COUNT(t) FROM Teacher t", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public long getTotalCourses() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Course c", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public long getTotalEnrollments() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT COUNT(e) FROM Enrollment e", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Get revenue grouped by month for the last X months.
     */
    public Map<String, BigDecimal> getMonthlyRevenue(int monthsCount) {
        EntityManager em = Jpa.em();
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        
        LocalDate now = LocalDate.now();
        for (int i = monthsCount - 1; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            String label = d.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy"));
            map.put(label, BigDecimal.ZERO);
        }

        try {
            LocalDate sixMonthsAgo = now.minusMonths(monthsCount - 1).withDayOfMonth(1);
            String q = "SELECT i.issueDate, i.totalAmount FROM Invoice i " +
                       "WHERE i.status = :status AND i.issueDate >= :from";
            
            List<Object[]> results = em.createQuery(q, Object[].class)
                    .setParameter("status", vn.edu.ute.languagecenter.management.model.Invoice.InvoiceStatus.Paid)
                    .setParameter("from", sixMonthsAgo)
                    .getResultList();

            for (Object[] row : results) {
                LocalDate date = (LocalDate) row[0];
                BigDecimal amount = (row[1] instanceof BigDecimal) ? (BigDecimal) row[1] : new BigDecimal(row[1].toString());
                
                String label = date.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy"));
                if (map.containsKey(label)) {
                    map.put(label, map.get(label).add(amount));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return map;
    }

    /**
     * Get Pass vs Fail ratio for Pie Chart.
     * Returns a map with keys "Đạt" and "Trượt".
     */
    public Map<String, Long> getPassFailRatio() {
        EntityManager em = Jpa.em();
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("Đạt", 0L);
        map.put("Trượt", 0L);
        
        try {
            // Since Result doesn't have a status field, we use score >= 5.0 as 'Đạt'
            String q = "SELECT (CASE WHEN r.score >= 5.0 THEN 'Đạt' ELSE 'Trượt' END), COUNT(r) " +
                       "FROM Result r GROUP BY (CASE WHEN r.score >= 5.0 THEN 'Đạt' ELSE 'Trượt' END)";
            List<Object[]> results = em.createQuery(q, Object[].class).getResultList();
            for (Object[] row : results) {
                String label = (String) row[0];
                Long count = (Long) row[1];
                if (label != null) {
                    map.put(label, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return map;
    }
}
