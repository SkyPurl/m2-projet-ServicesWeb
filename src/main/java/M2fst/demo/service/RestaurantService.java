package M2fst.demo.service;

import M2fst.demo.dto.ImageDto;
import M2fst.demo.dto.ImageUploadDto;
import M2fst.demo.dto.RestaurantCreationDto;
import M2fst.demo.dto.RestaurantDto;
import M2fst.demo.entity.EvaluationEntity;
import M2fst.demo.entity.RestaurantEntity;
import M2fst.demo.exception.NotFoundException;
import M2fst.demo.repository.RestaurantRepository;
import M2fst.demo.repository.S3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final S3Repository s3Repository;

    @Value("${s3.buckets.restaurant}")
    private String bucket;

    /**
     * Crée un restaurant à partir d'un DTO de création de restaurant
     *
     * @param dto DTO avec les infos de création du restaurant
     * @return DTO du restaurant créé
     */
    public RestaurantDto creerRestaurant(RestaurantCreationDto dto) {
        RestaurantEntity entity = new RestaurantEntity();
        entity.setNom(dto.getNom());
        entity.setAdresse(dto.getAdresse());
        entity = restaurantRepository.save(entity);
        return versDto(entity);
    }

    /**
     * Récupère un restaurant à partir de son identifiant
     *
     * @param id identifiant du restaurant
     * @return DTO du restaurant
     * @throws NotFoundException si le restaurant n'existe pas
     */
    public RestaurantDto recupererRestaurant(Integer id) {
        RestaurantEntity entity = trouverRestaurant(id);
        return versDto(entity);
    }

    /**
     * Récupère tous les restaurants
     *
     * @return Liste de DTO de tous les restaurants
     */
    public List<RestaurantDto> recupererTousLesRestaurants() {
        List<RestaurantEntity> entities = restaurantRepository.findAll();
        return entities.stream()
                .map(this::versDto)
                .toList();
    }

    /**
     * Modifier un restaurant
     *
     * @param id  id du restaurant à modifier
     * @param dto informations à modifier du restaurant
     * @return DTO du restaurant
     */
    public RestaurantDto modifierRestaurant(Integer id, RestaurantCreationDto dto) {
        RestaurantEntity entity = trouverRestaurant(id);
        entity.setNom(dto.getNom());
        entity.setAdresse(dto.getAdresse());
        entity = restaurantRepository.save(entity);
        return versDto(entity);
    }

    public ImageUploadDto ajouterImage(Integer id){
        RestaurantEntity entity = trouverRestaurant(id);
        UUID imageId = UUID.randomUUID();
        entity.setImageId(imageId);
        restaurantRepository.save(entity);
        String url = s3Repository.getUploadUrl(bucket, imageId.toString());
        return new ImageUploadDto(url);
    }

    public ImageDto recupererImage(Integer id){
        RestaurantEntity entity = trouverRestaurant(id);
        if(entity.getImageId() == null) throw new NotFoundException("ce restaurant n'a pas d'image");
        String url = s3Repository.getFileUrl(bucket, entity.getImageId().toString());
        return new ImageDto(url);
    }

    /**
     * Trouve un restaurant par son identifiant
     *
     * @param id identifiant du restaurant
     * @return l'entité du restaurant
     * @throws NotFoundException si le restaurant n'existe pas
     */
    public RestaurantEntity trouverRestaurant(Integer id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant avec l'id " + id + " non trouvé"));
    }

    /**
     * Calcule la moyenne des notes d'un restaurant à partir de ses évaluations
     *
     * @param restaurant entité du restaurant
     * @return la moyenne des notes ou -1 si aucune évaluation n'existe
     */
    public double calculerMoyenne(RestaurantEntity restaurant) {
        return restaurant.getEvaluations().stream()
                .mapToInt(EvaluationEntity::getNote)
                .average()
                .orElse(-1.0);
    }

    /**
     * Mapping Entity vers Dto avec calcul de la moyenne
     *
     * @param restaurant entity
     * @return DTO du restaurant
     */
    private RestaurantDto versDto(RestaurantEntity restaurant) {
        return RestaurantDto.fromEntity(restaurant, calculerMoyenne(restaurant));
    }
}
