package M2fst.demo.repository;

import M2fst.demo.entity.EvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité EvaluationEntity.
 */

@Repository
public interface EvaluationRepository extends JpaRepository<EvaluationEntity, Integer> {
    List<EvaluationEntity> findByAuteurId(String auteurId);
}
