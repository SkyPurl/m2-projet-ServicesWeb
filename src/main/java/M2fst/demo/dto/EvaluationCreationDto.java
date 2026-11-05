package M2fst.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationCreationDto {

    // nom de l'évaluateur
    @NotBlank
    @Size(max = 50)
    private String nomEvaluateur;
    // commentaire de l'évaluation
    @NotBlank
    @Size(max = 255)
    private String commentaire;
    // note de l'évaluation
    @NotNull
    @Min(0)
    @Max(3)
    private Integer note;
}
