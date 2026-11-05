package M2fst.demo.service;

import M2fst.demo.dto.EvaluationCreationDto;
import M2fst.demo.dto.EvaluationDto;
import M2fst.demo.dto.ImageDto;
import M2fst.demo.dto.ImageUploadDto;
import M2fst.demo.entity.EvaluationEntity;
import M2fst.demo.entity.RestaurantEntity;
import M2fst.demo.exception.ForbiddenException;
import M2fst.demo.exception.NotFoundException;
import M2fst.demo.repository.EvaluationRepository;
import M2fst.demo.repository.S3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EvaluationRepository evaluationRepository;
    private final RestaurantService restaurantService;
    private final S3Repository s3Repository;
    private final IndexService indexService;

    @Value("${s3.buckets.evaluation}")
    private String bucket;

    /**
     * Ajoute une évaluation pour un restaurant donné
     *
     * @param restaurantId id du restaurant
     * @param dto          données de l'évaluation
     * @param auteurId     id de l'auteur
     * @return l'évaluation créée
     */
    public EvaluationDto ajouterEvaluation(Integer restaurantId, EvaluationCreationDto dto, String auteurId) {
        RestaurantEntity restaurant = restaurantService.trouverRestaurant(restaurantId);
        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setNomEvaluateur(dto.getNomEvaluateur());
        evaluation.setNote(dto.getNote());
        evaluation.setCommentaire(dto.getCommentaire());
        evaluation.setRestaurant(restaurant);
        evaluation.setAuteurId(auteurId);
        evaluation = this.evaluationRepository.save(evaluation);
        indexService.indexerEvaluation(evaluation.getId(), evaluation.getCommentaire());
        return EvaluationDto.fromEntity(evaluation);
    }

    /**
     * Récupère toutes les évaluations pour un auteur donné
     *
     * @param auteurId id de l'auteur
     * @return la liste des évaluations
     */
    public List<EvaluationDto> recupererEvaluationsParAuteur(String auteurId) {
        return this.evaluationRepository.findByAuteurId(auteurId).stream()
                .map(EvaluationDto::fromEntity)
                .toList();
    }

    /**
     * Supprime une évaluation
     *
     * @param evaluationId  id de l'évaluation
     * @param idUtilisateur id de l'auteur
     * @param estAdmin      indique si l'utilisateur est administrateur
     * @return l'évaluation supprimée
     */
    public EvaluationDto supprimerEvaluation(Integer evaluationId, String idUtilisateur, boolean estAdmin) {
        EvaluationEntity evaluation = trouverEvaluation(evaluationId);
        verifierDroits(evaluation, idUtilisateur, estAdmin);
        EvaluationDto evaluationDto = EvaluationDto.fromEntity(evaluation);
        this.evaluationRepository.delete(evaluation);
        indexService.supprimerEvaluation(evaluationId);
        return evaluationDto;
    }

    /**
     * Modifie une évaluation
     *
     * @param evaluationId  id de l'évaluation
     * @param dto           données de l'évaluation
     * @param idUtilisateur id de l'utilisateur
     * @param estAdmin      indique si l'utilisateur est administrateur
     * @return l'évaluation modifiée
     */
    public EvaluationDto modifierEvaluation(Integer evaluationId, EvaluationCreationDto dto, String idUtilisateur, boolean estAdmin) {
        EvaluationEntity evaluation = trouverEvaluation(evaluationId);
        verifierDroits(evaluation, idUtilisateur, estAdmin);
        evaluation.setNomEvaluateur(dto.getNomEvaluateur());
        evaluation.setNote(dto.getNote());
        evaluation.setCommentaire(dto.getCommentaire());
        evaluation = this.evaluationRepository.save(evaluation);
        indexService.indexerEvaluation(evaluation.getId(), evaluation.getCommentaire());
        return EvaluationDto.fromEntity(evaluation);
    }

    /**
     * méthode qui permet d'ajouter une photo à une évaluation
     * @param evaluationId id de l'évaluation
     * @param idUtilisateur id de l'utilisateur qui veut ajouter la photo
     * @param estAdmin est-ce que l'utilisateur est admin ou pas
     * @return retourne le lien pour uploader la photo
     */
    public ImageUploadDto ajouterPhoto(Integer evaluationId, String idUtilisateur, boolean estAdmin) {
        EvaluationEntity evaluation = trouverEvaluation(evaluationId);
        verifierDroits(evaluation, idUtilisateur, estAdmin);
        UUID uuid = UUID.randomUUID();
        evaluation.getImageIds().add(uuid);
        evaluationRepository.save(evaluation);
        String cle = construireCle(uuid, evaluationId);
        String url = s3Repository.getUploadUrl(bucket, cle);
        return new ImageUploadDto(url);
    }

    /**
     * méthode qui permet de récupérer les photos d'une évaluation
     * @param evaluationId id de l'évaluation dont on souhaite les photos
     * @return la liste des urls des photos de l'évaluation
     */
    public List<ImageDto> recupererPhotos(Integer evaluationId) {
        EvaluationEntity evaluation = trouverEvaluation(evaluationId);
        return evaluation.getImageIds().stream()
                .map(idPhoto -> {
                    String cle = construireCle(idPhoto, evaluationId);
                    String url = s3Repository.getFileUrl(bucket, cle);
                    return new ImageDto(url);
                })
                .toList();
    }

    /**
     * méthode qui permet de retourner toutes les évaluations correspondant au(x) mot(s) recherché(s)
     * @param motsCles la ou les mots clés recherchés
     * @return la liste des évaluations avec ces mots clés
     */
    public List<EvaluationDto> rechercherEvaluations(String motsCles) {
        if (motsCles == null || motsCles.isBlank()) return List.of();

        List<Integer> evaluationsIds = indexService.rechercher(motsCles);
        return evaluationRepository.findAllById(evaluationsIds).stream()
                .map(EvaluationDto::fromEntity)
                .toList();
    }

    /**
     * Récupère une évaluation par son id
     *
     * @param id id de l'évaluation
     * @return l'évaluation trouvée
     */
    private EvaluationEntity trouverEvaluation(Integer id) {
        return this.evaluationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Evaluation avec l'id " + id + " non trouvé"));
    }

    /**
     * Vérifie si l'utilisateur a les droits pour supprimer/modifier une évaluation
     *
     * @param evaluation evaluation pour laquelle on veut vérifier les droits
     * @param auteurId   id de l'auteur
     * @param estAdmin   indique si l'utilisateur est administrateur
     * @throws ForbiddenException si l'utilisateur n'a pas les droits
     */
    private void verifierDroits(EvaluationEntity evaluation, String auteurId, boolean estAdmin) {
        if (!Objects.equals(evaluation.getAuteurId(), auteurId) && !estAdmin) {
            throw new ForbiddenException("Vous n'avez pas les droits pour supprimer/modifier cette évaluation");
        }
    }

    private String construireCle(UUID imageId, Integer evaluationID) {
        return evaluationID + "/" + imageId;
    }
}
