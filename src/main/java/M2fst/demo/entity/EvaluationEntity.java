package M2fst.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une évaluation d'un restaurant.
 */

@Getter
@Setter
@Entity
@Table(name = "Evaluation")
public class EvaluationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // id d'une évaluation
    private Integer id;
    @Column(name = "nom_evaluateur", length = 50, nullable = false)
    // nom de la personne qui a fait l'évaluation
    private String nomEvaluateur;
    @Column(name = "commentaire", length = 255)
    // commentaire de l'évaluation
    private String commentaire;
    @Column(name = "note", nullable = false)
    // note de l'évaluation
    private Integer note;
    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id")
    // restaurant évalué
    private RestaurantEntity restaurant;
    @Column(name = "auteur_id", nullable = false)
    // identifiant de l'auteur
    private String auteurId;
    @ElementCollection
    @CollectionTable(name = "evaluation_image", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "image_id")
    // liste des identifiants des images associées à l'évaluation
    private List<UUID> imageIds = new ArrayList<>();
}
