package M2fst.demo;

import M2fst.demo.dto.ImageUploadDto;
import M2fst.demo.dto.RestaurantCreationDto;
import M2fst.demo.dto.RestaurantDto;
import M2fst.demo.entity.EvaluationEntity;
import M2fst.demo.entity.RestaurantEntity;
import M2fst.demo.exception.NotFoundException;
import M2fst.demo.repository.RestaurantRepository;
import M2fst.demo.repository.S3Repository;
import M2fst.demo.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Classe de tests pour le service Restaurant
 */

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private S3Repository s3Repository;

    private RestaurantEntity restaurant;

    private RestaurantCreationDto dto;

    @BeforeEach
    public void setup() {
        restaurant = new RestaurantEntity();
        restaurant.setId(1);
        restaurant.setNom("Ezel Kebab");
        restaurant.setAdresse("13 rue du général patton");

        dto = new RestaurantCreationDto();
        dto.setNom("Ezel Kebab");
        dto.setAdresse("13 rue du général patton");

        ReflectionTestUtils.setField(restaurantService, "bucket", "restaurant");
    }

    // creer une évaluation avec la note en paramètre
    private EvaluationEntity creerEvaluation(int note){
        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setNote(note);
        evaluation.setRestaurant(restaurant);
        return evaluation;
    }

    @Test
    void calculerMoyenne_sansEvaluation_retourneMoinsUn(){
        double moyenne = restaurantService.calculerMoyenne(restaurant);

        assertEquals(-1.0,moyenne);
    }

    @Test
    void calculerMoyenne_uneEvaluation_retourneSaNote(){
        restaurant.getEvaluations().add(creerEvaluation(3));
        double moyenne = restaurantService.calculerMoyenne(restaurant);

        assertEquals(3.0,moyenne);
    }

    @Test
    void calculerMoyenne_plusieursEvaluations_retourneSaMoyenne(){
        restaurant.getEvaluations().add(creerEvaluation(3));
        restaurant.getEvaluations().add(creerEvaluation(2));
        double moyenne = restaurantService.calculerMoyenne(restaurant);

        assertEquals(2.5,moyenne);
    }

    @Test
    void creerRestaurant_dtoValide_retourneRestaurantCree(){
        when(restaurantRepository.save(any(RestaurantEntity.class))).thenReturn(restaurant);

        RestaurantDto resultat = restaurantService.creerRestaurant(dto);

        assertEquals(1,resultat.getId());
        assertEquals("Ezel Kebab",resultat.getNom());
        assertEquals(-1.0,resultat.getMoyenne());
        verify(restaurantRepository).save(any(RestaurantEntity.class));
    }

    @Test
    void recupererRestaurant_idExistant_retourneRestaurantAvecMoyenne(){
        restaurant.getEvaluations().add(creerEvaluation(3));
        restaurant.getEvaluations().add(creerEvaluation(2));
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        RestaurantDto resultat = restaurantService.recupererRestaurant(1);

        assertEquals("Ezel Kebab",resultat.getNom());
        assertEquals(2.5,resultat.getMoyenne());
        assertEquals(2,resultat.getEvaluations().size());
    }

    @Test
    void restaurantInexistant_leveNotFoundException(){
        when(restaurantRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> restaurantService.recupererRestaurant(99));
        assertThrows(NotFoundException.class, () -> restaurantService.modifierRestaurant(99, dto));
        assertThrows(NotFoundException.class, () -> restaurantService.ajouterImage(99));
        assertThrows(NotFoundException.class, () -> restaurantService.recupererImage(99));
        verify(restaurantRepository, never()).save(any(RestaurantEntity.class));
        verify(s3Repository, never()).getUploadUrl(anyString(), anyString());
    }

    @Test
    void recupererTousLesRestaurants_retourneTousLesRestaurants(){
        RestaurantEntity pizzeria = new RestaurantEntity();
        pizzeria.setId(2);
        pizzeria.setNom("La Tour De Pizz'");
        pizzeria.setAdresse("34 rue du général patton");
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant, pizzeria));

        List<RestaurantDto> resultat = restaurantService.recupererTousLesRestaurants();

        assertEquals(2,resultat.size());
        assertEquals("La Tour De Pizz'",resultat.get(1).getNom());
    }

    @Test
    void modifierRestaurant_idExistant_modifieNomEtAdresse(){
        dto.setNom("Ezel Kebab Patton");
        dto.setAdresse("15 rue du général patton");
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        RestaurantDto resultat = restaurantService.modifierRestaurant(1, dto);

        assertEquals("Ezel Kebab Patton",resultat.getNom());
        assertEquals("15 rue du général patton",resultat.getAdresse());
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void ajouterImage_idExistant_retourneUrlUpload(){
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(s3Repository.getUploadUrl(anyString(), anyString())).thenReturn("https://s3/upload");

        ImageUploadDto resultat = restaurantService.ajouterImage(1);

        assertEquals("https://s3/upload",resultat.getUrl());
        assertNotNull(restaurant.getImageId());
        verify(s3Repository).getUploadUrl("restaurant", restaurant.getImageId().toString());
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void ajouterImage_imageDejaPresente_remplaceImage(){
        UUID ancienneImage = UUID.randomUUID();
        restaurant.setImageId(ancienneImage);
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(s3Repository.getUploadUrl(anyString(), anyString())).thenReturn("https://s3/upload");

        restaurantService.ajouterImage(1);

        assertNotEquals(ancienneImage,restaurant.getImageId());
    }

    @Test
    void recupererImage_avecImage_retourneUrl(){
        UUID imageId = UUID.randomUUID();
        restaurant.setImageId(imageId);
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(s3Repository.getFileUrl("restaurant", imageId.toString())).thenReturn("https://s3/image");

        assertEquals("https://s3/image",restaurantService.recupererImage(1).getUrl());
    }

    @Test
    void recupererImage_sansImage_leveNotFoundException(){
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        assertThrows(NotFoundException.class, () -> restaurantService.recupererImage(1));
        verify(s3Repository, never()).getFileUrl(anyString(), anyString());
    }
}