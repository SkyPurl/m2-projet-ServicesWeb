package M2fst.demo.controller;

import M2fst.demo.dto.EvaluationCreationDto;
import M2fst.demo.dto.EvaluationDto;
import M2fst.demo.dto.ImageDto;
import M2fst.demo.dto.ImageUploadDto;
import M2fst.demo.service.EvaluationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "bearer-key")
public class EvaluationController {
    private final EvaluationService evaluationService;

    // Ajoute une évaluation pour un restaurant donné
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/restaurants/{id}/evaluations")
    public EvaluationDto ajouterEvaluation(@PathVariable(name = "id") Integer restaurantId, @Valid @RequestBody EvaluationCreationDto dto, Authentication authentication) {
        return this.evaluationService.ajouterEvaluation(restaurantId, dto, authentication.getName());
    }

    // récupère toutes mes évaluations
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/evaluations/moi")
    public List<EvaluationDto> recupererEvaluationsParAuteur(Authentication authentication) {
        return this.evaluationService.recupererEvaluationsParAuteur(authentication.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/evaluations/{id}")
    public EvaluationDto modifierEvaluation(@PathVariable(name = "id") Integer evaluationId, @Valid @RequestBody EvaluationCreationDto dto, Authentication authentication) {
        boolean estAdmin = estAdmin(authentication);
        return this.evaluationService.modifierEvaluation(evaluationId, dto, authentication.getName(), estAdmin);
    }

    // Supprime une évaluation
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/evaluations/{id}")
    public EvaluationDto supprimerEvaluation(@PathVariable(name = "id") Integer evaluationId, Authentication authentication) {
        boolean estAdmin = estAdmin(authentication);
        return this.evaluationService.supprimerEvaluation(evaluationId, authentication.getName(), estAdmin);
    }

    // Ajoute une photo à une évaluation
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/evaluations/{id}/photos")
    public ImageUploadDto ajouterPhoto(@PathVariable(name = "id") Integer evaluationId, Authentication authentication) {
        boolean  estAdmin = estAdmin(authentication);
        return this.evaluationService.ajouterPhoto(evaluationId, authentication.getName(), estAdmin);
    }

    // recupère toutes les photos d'une évaluation
    @GetMapping("/evaluations/{id}/photos")
    public List<ImageDto> recupererPhotos(@PathVariable(name = "id") Integer evaluationId) {
        return this.evaluationService.recupererPhotos(evaluationId);
    }

    // permet de rechercher les évaluations contenant le ou les mots clés
    @GetMapping("/evaluations/recherche")
    public List<EvaluationDto> rechercherEvaluations(@RequestParam(name = "motsCles") String motsCles){
        return this.evaluationService.rechercherEvaluations(motsCles);
    }

    private boolean estAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
    }
}
