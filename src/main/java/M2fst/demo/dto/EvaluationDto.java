package M2fst.demo.dto;

import M2fst.demo.entity.EvaluationEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EvaluationDto {

    // identifiant de l'évaluation
    private Integer id;
    // nom de l'évaluateur
    private String nomEvaluateur;
    // commentaire de l'évaluation
    private String commentaire;
    // note de l'évaluation
    private Integer note;
    // identifiant du restaurant évalué
    private Integer restaurantId;
    // liste des identifiants des images de l'évaluation
    private List<UUID> imageIds;

    public static EvaluationDto fromEntity(EvaluationEntity evaluationEntity){
        EvaluationDto evaluationDto = new EvaluationDto();
        evaluationDto.setId(evaluationEntity.getId());
        evaluationDto.setNomEvaluateur(evaluationEntity.getNomEvaluateur());
        evaluationDto.setCommentaire(evaluationEntity.getCommentaire());
        evaluationDto.setNote(evaluationEntity.getNote());
        evaluationDto.setRestaurantId(evaluationEntity.getRestaurant().getId());
        evaluationDto.setImageIds(List.copyOf(evaluationEntity.getImageIds()));
        return evaluationDto;
    }
}
