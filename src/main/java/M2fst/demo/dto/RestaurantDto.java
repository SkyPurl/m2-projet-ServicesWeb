package M2fst.demo.dto;

import M2fst.demo.entity.RestaurantEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestaurantDto {

    // identifiant du restaurant
    private Integer id;
    // nom du restaurant
    private String nom;
    // adresse du restaurant
    private String adresse;
    // note moyenne du restaurant
    private Double moyenne;
    // liste des évaluations du restaurant
    private List<EvaluationDto> evaluations;

    public static RestaurantDto fromEntity(RestaurantEntity restaurantEntity, double moyenne){
        RestaurantDto restaurantDto = new RestaurantDto();
        restaurantDto.setId(restaurantEntity.getId());
        restaurantDto.setNom(restaurantEntity.getNom());
        restaurantDto.setAdresse(restaurantEntity.getAdresse());
        restaurantDto.setMoyenne(moyenne);
        restaurantDto.setEvaluations(restaurantEntity.getEvaluations().stream().map(EvaluationDto::fromEntity).toList());
        return restaurantDto;
    }
}
