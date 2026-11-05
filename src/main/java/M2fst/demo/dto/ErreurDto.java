package M2fst.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErreurDto {

    // code HTTP de l'erreur
    private Integer code;
    // message d'erreur
    private String message;
}
