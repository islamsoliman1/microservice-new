package com.programming.techie.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);
        if (realmAccess != null && realmAccess.get(ROLES) instanceof Collection<?> roles) {
            authorities.addAll(
                    roles.stream()
                            .map(Object::toString)
                            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                            .collect(Collectors.toList())
            );
        }

        String scope = jwt.getClaimAsString("scope");
        if (scope != null && !scope.isBlank()) {
            for (String s : scope.split(" ")) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
            }
        }

        return authorities;
    }
}