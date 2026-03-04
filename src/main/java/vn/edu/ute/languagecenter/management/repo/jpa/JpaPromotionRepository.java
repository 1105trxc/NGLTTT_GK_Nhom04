package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.model.Promotion;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.repo.PromotionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaPromotionRepository implements PromotionRepository {

    private final TransactionManager tm = new TransactionManager();

    @Override
    public Promotion save(Promotion promotion) {
        try {
            return tm.runInTransaction(em -> {
                if (promotion.getPromotionId() == null) {
                    em.persist(promotion);
                    return promotion;
                }
                return em.merge(promotion);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu Promotion", e);
        }
    }

    @Override
    public Optional<Promotion> findById(Long id) {
        try {
            return tm.runInTransaction(em ->
                    Optional.ofNullable(em.find(Promotion.class, id))
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findById Promotion", e);
        }
    }

    @Override
    public List<Promotion> findAll() {
        try {
            return tm.runInTransaction(em ->
                    em.createQuery("SELECT p FROM Promotion p ORDER BY p.startDate DESC",
                            Promotion.class).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findAll Promotion", e);
        }
    }

    @Override
    public List<Promotion> findAllActive() {
        LocalDate today = LocalDate.now();
        return findAll().stream()
                .filter(p -> p.getStatus() == Promotion.ActiveStatus.Active)
                .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(today))
                .filter(p -> p.getEndDate()   == null || !p.getEndDate().isBefore(today))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        try {
            tm.runInTransaction(em -> {
                Promotion p = em.find(Promotion.class, id);
                if (p != null) em.remove(p);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa Promotion", e);
        }
    }
}