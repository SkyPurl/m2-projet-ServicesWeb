package M2fst.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant un restaurant.
 */

@Getter
@Setter
@Entity
@Table(name = "Restaurant")
public class RestaurantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // id du restaurant
    private Integer id;
    @Column(name = "nom", length = 90, nullable = false)
    // nom du restaurant
    private String nom;
    @Column(name = "adresse", length = 255, nullable = false)
    // adresse du restaurant
    private String adresse;
    @Column(name = "image_id")
    // id de l'image du restaurant
    private UUID imageId;
    @OneToMany(mappedBy = "restaurant")
    // liste des évaluations du restaurant
    private List<EvaluationEntity> evaluations = new ArrayList<>();
}
