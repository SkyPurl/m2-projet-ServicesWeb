package M2fst.demo.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Cette méthode convertit un JWT en une collection de rôles
     * @param source le JWT de l'utilisateur
     * @return la collection des rôles de l'utilisateur
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // vérification que le JWT contient bien ressourse_access
        Map<String, Object> resourceAccess = source.getClaimAsMap("resource_access");
        if (resourceAccess == null) {
            return authorities;
        }
        // vérification que ressource_access contient bien coursm2
        Map<String, Object> coursm2 = (Map<String, Object>) resourceAccess.get("coursm2");
        if (coursm2 == null) {
            return authorities;
        }
        // récupération des rôles depuis coursm2
        List<String> roles = (List<String>) coursm2.get("roles");
        if (roles == null) {
            return authorities;
        }
        // conversion des rôles en GrantedAuthority
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .toList();

    }
}
