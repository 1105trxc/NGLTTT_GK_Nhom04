package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Promotion;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository {
    Promotion save(Promotion promotion);
    Optional<Promotion> findById(Long id);
    List<Promotion> findAll();
    List<Promotion> findAllActive();
    void deleteById(Long id);
}
