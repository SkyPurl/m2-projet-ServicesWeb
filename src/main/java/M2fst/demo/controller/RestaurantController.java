package M2fst.demo.controller;

import M2fst.demo.dto.ImageDto;
import M2fst.demo.dto.ImageUploadDto;
import M2fst.demo.dto.RestaurantCreationDto;
import M2fst.demo.dto.RestaurantDto;
import M2fst.demo.service.RestaurantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "bearer-key")
public class RestaurantController {
    private final RestaurantService restaurantService;

    // Récupère tous les restaurants
    @GetMapping("/restaurants")
    public List<RestaurantDto> recupererRestaurants() {
        return this.restaurantService.recupererTousLesRestaurants();
    }

    // Récupère un restaurant par son identifiant
    @GetMapping("/restaurants/{id}")
    public RestaurantDto recupererRestaurant(@PathVariable(name = "id") Integer id) {
        return this.restaurantService.recupererRestaurant(id);
    }

    // Crée un restaurant
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/restaurants")
    public RestaurantDto creerRestaurant(@Valid @RequestBody RestaurantCreationDto restaurant) {
        return this.restaurantService.creerRestaurant(restaurant);
    }

    // Modifie un restaurant
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restaurants/{id}")
    public RestaurantDto modifierRestaurant(@PathVariable(name = "id") Integer id, @Valid @RequestBody RestaurantCreationDto restaurant) {
        return this.restaurantService.modifierRestaurant(id, restaurant);
    }

    //Ajouter une image pour un restaurant
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restaurants/{id}/image")
    public ImageUploadDto ajouterImage(@PathVariable(name = "id") Integer id){
        return this.restaurantService.ajouterImage(id);
    }

    // Recuperer l'image d'un restaurant
    @GetMapping("/restaurants/{id}/image")
    public ImageDto recupererImage(@PathVariable(name = "id") Integer id){
        return this.restaurantService.recupererImage(id);
    }

}
