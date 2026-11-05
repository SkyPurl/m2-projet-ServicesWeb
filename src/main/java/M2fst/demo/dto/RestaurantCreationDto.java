package M2fst.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantCreationDto {

    // nom du restaurant
    @NotBlank
    @Size(max = 90)
    private String nom;
    // adresse du restaurant
    @NotBlank
    @Size(max = 255)
    private String adresse;
}
