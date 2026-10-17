package com.pawsclaws.petshop.security;

import com.pawsclaws.petshop.domain.AppUser;
import com.pawsclaws.petshop.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Authenticated principal. Carries the tenant (petshop) id so downstream
 * services can scope every query without trusting request parameters.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final Long petshopId;
    private final String petshopName;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean enabled;

    public UserPrincipal(Long id,
                         Long petshopId,
                         String petshopName,
                         String name,
                         String email,
                         String password,
                         Role role,
                         boolean enabled) {
        this.id = id;
        this.petshopId = petshopId;
        this.petshopName = petshopName;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    public static UserPrincipal from(AppUser user) {
        return new UserPrincipal(
                user.getId(),
                user.getPetshop().getId(),
                user.getPetshop().getName(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.isActive());
    }

    public Long getId() {
        return id;
    }

    public Long getPetshopId() {
        return petshopId;
    }

    public String getPetshopName() {
        return petshopName;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
