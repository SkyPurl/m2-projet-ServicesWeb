package M2fst.demo;

import M2fst.demo.dto.EvaluationCreationDto;
import M2fst.demo.dto.EvaluationDto;
import M2fst.demo.dto.ImageUploadDto;
import M2fst.demo.entity.EvaluationEntity;
import M2fst.demo.entity.RestaurantEntity;
import M2fst.demo.exception.ForbiddenException;
import M2fst.demo.exception.NotFoundException;
import M2fst.demo.repository.EvaluationRepository;
import M2fst.demo.repository.S3Repository;
import M2fst.demo.service.EvaluationService;
import M2fst.demo.service.IndexService;
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
 * Classe de tests pour le service Evaluation
 */

@ExtendWith(MockitoExtension.class)
public class EvaluationServiceTest {

    @InjectMocks
    private EvaluationService evaluationService;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private S3Repository s3Repository;

    @Mock
    private IndexService indexService;

    private RestaurantEntity restaurant;

    private EvaluationEntity evaluation;

    private EvaluationCreationDto dto;

    @BeforeEach
    public void setup() {
        restaurant = new RestaurantEntity();
        restaurant.setId(1);
        restaurant.setNom("Ezel Kebab");
        restaurant.setAdresse("13 rue du général patton");

        evaluation = new EvaluationEntity();
        evaluation.setId(1);
        evaluation.setNomEvaluateur("Lucien");
        evaluation.setCommentaire("très bon kebab frites");
        evaluation.setNote(3);
        evaluation.setRestaurant(restaurant);
        evaluation.setAuteurId("lucien");

        dto = new EvaluationCreationDto();
        dto.setNomEvaluateur("Lucien");
        dto.setCommentaire("pas mal d'attente");
        dto.setNote(2);

        ReflectionTestUtils.setField(evaluationService, "bucket", "evaluation");
    }

    @Test
    void ajouterEvaluation_restaurantExistant_retourneEvaluation(){
        when(restaurantService.trouverRestaurant(1)).thenReturn(restaurant);
        when(evaluationRepository.save(any(EvaluationEntity.class))).thenReturn(evaluation);

        EvaluationDto resultat = evaluationService.ajouterEvaluation(1, dto, "lucien");

        assertEquals(1,resultat.getId());
        assertEquals(1,resultat.getRestaurantId());
        verify(evaluationRepository).save(any(EvaluationEntity.class));
        verify(indexService).indexerEvaluation(1, "très bon kebab frites");
    }

    @Test
    void ajouterEvaluation_restaurantInexistant_leveNotFoundException(){
        when(restaurantService.trouverRestaurant(99)).thenThrow(new NotFoundException("Restaurant avec l'id 99 non trouvé"));

        assertThrows(NotFoundException.class, () -> evaluationService.ajouterEvaluation(99, dto, "lucien"));
        verify(evaluationRepository, never()).save(any(EvaluationEntity.class));
        verify(indexService, never()).indexerEvaluation(any(), anyString());
    }

    @Test
    void recupererEvaluationsParAuteur_retourneSesEvaluations(){
        when(evaluationRepository.findByAuteurId("lucien")).thenReturn(List.of(evaluation));

        List<EvaluationDto> resultat = evaluationService.recupererEvaluationsParAuteur("lucien");

        assertEquals(1,resultat.size());
        assertEquals("très bon kebab frites",resultat.get(0).getCommentaire());
    }

    @Test
    void evaluationInexistante_leveNotFoundException(){
        when(evaluationRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> evaluationService.supprimerEvaluation(99, "lucien", false));
        assertThrows(NotFoundException.class, () -> evaluationService.modifierEvaluation(99, dto, "lucien", false));
        assertThrows(NotFoundException.class, () -> evaluationService.ajouterPhoto(99, "lucien", false));
        assertThrows(NotFoundException.class, () -> evaluationService.recupererPhotos(99));
        verify(evaluationRepository, never()).delete(any(EvaluationEntity.class));
    }

    @Test
    void supprimerEvaluation_parAuteur_supprimeEvaluation(){
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));

        EvaluationDto resultat = evaluationService.supprimerEvaluation(1, "lucien", false);

        assertEquals(1,resultat.getId());
        verify(evaluationRepository).delete(evaluation);
        verify(indexService).supprimerEvaluation(1);
    }

    @Test
    void supprimerEvaluation_parAdmin_supprimeEvaluation(){
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));

        evaluationService.supprimerEvaluation(1, "noel", true);

        verify(evaluationRepository).delete(evaluation);
    }

    @Test
    void supprimerEvaluation_parAutreUtilisateur_leveForbiddenException(){
        evaluation.setAuteurId("noel");
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));

        assertThrows(ForbiddenException.class, () -> evaluationService.supprimerEvaluation(1, "lucien", false));
        verify(evaluationRepository, never()).delete(any(EvaluationEntity.class));
        verify(indexService, never()).supprimerEvaluation(any());
    }

    @Test
    void modifierEvaluation_parAuteur_modifieEvaluation(){
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        EvaluationDto resultat = evaluationService.modifierEvaluation(1, dto, "lucien", false);

        assertEquals("pas mal d'attente",resultat.getCommentaire());
        assertEquals(2,resultat.getNote());
        verify(indexService).indexerEvaluation(1, "pas mal d'attente");
    }

    @Test
    void modifierEvaluation_parAutreUtilisateur_leveForbiddenException(){
        evaluation.setAuteurId("noel");
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));

        assertThrows(ForbiddenException.class, () -> evaluationService.modifierEvaluation(1, dto, "lucien", false));
        assertEquals("très bon kebab frites",evaluation.getCommentaire());
        verify(evaluationRepository, never()).save(any(EvaluationEntity.class));
    }

    @Test
    void ajouterPhoto_parAuteur_retourneUrlUpload(){
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));
        when(s3Repository.getUploadUrl(anyString(), anyString())).thenReturn("https://s3/upload");

        ImageUploadDto resultat = evaluationService.ajouterPhoto(1, "lucien", false);

        assertEquals("https://s3/upload",resultat.getUrl());
        assertEquals(1,evaluation.getImageIds().size());
        verify(s3Repository).getUploadUrl("evaluation", "1/" + evaluation.getImageIds().get(0));
        verify(evaluationRepository).save(evaluation);
    }

    @Test
    void ajouterPhoto_parAutreUtilisateur_leveForbiddenException(){
        evaluation.setAuteurId("noel");
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));

        assertThrows(ForbiddenException.class, () -> evaluationService.ajouterPhoto(1, "lucien", false));
        assertTrue(evaluation.getImageIds().isEmpty());
        verify(s3Repository, never()).getUploadUrl(anyString(), anyString());
    }

    @Test
    void recupererPhotos_retourneUrlDesPhotos(){
        UUID photo1 = UUID.randomUUID();
        UUID photo2 = UUID.randomUUID();
        evaluation.getImageIds().add(photo1);
        evaluation.getImageIds().add(photo2);
        when(evaluationRepository.findById(1)).thenReturn(Optional.of(evaluation));
        when(s3Repository.getFileUrl("evaluation", "1/" + photo1)).thenReturn("https://s3/photo1");
        when(s3Repository.getFileUrl("evaluation", "1/" + photo2)).thenReturn("https://s3/photo2");

        assertEquals("https://s3/photo1",evaluationService.recupererPhotos(1).get(0).getUrl());
        assertEquals("https://s3/photo2",evaluationService.recupererPhotos(1).get(1).getUrl());
    }

    @Test
    void rechercherEvaluations_motsClesVides_retourneListeVide(){
        assertTrue(evaluationService.rechercherEvaluations(null).isEmpty());
        assertTrue(evaluationService.rechercherEvaluations("").isEmpty());
        verify(indexService, never()).rechercher(anyString());
    }

    @Test
    void rechercherEvaluations_motTrouve_retourneEvaluations(){
        when(indexService.rechercher("kebab")).thenReturn(List.of(1));
        when(evaluationRepository.findAllById(List.of(1))).thenReturn(List.of(evaluation));

        List<EvaluationDto> resultat = evaluationService.rechercherEvaluations("kebab");

        assertEquals(1,resultat.size());
        assertEquals(1,resultat.get(0).getId());
    }
}