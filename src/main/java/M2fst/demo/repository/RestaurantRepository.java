package M2fst.demo.repository;

import M2fst.demo.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour l'entité RestaurantEntity.
 */

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantEntity, Integer>{
}
