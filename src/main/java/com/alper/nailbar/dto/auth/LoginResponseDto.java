package com.alper.nailbar.dto.auth;

import com.alper.nailbar.model.enums.Role;

/**
 * Başarılı login sonrasında frontend'e döndürülen yanıt.
 * Frontend bu token'ı sonraki isteklerde Authorization: Bearer <token> header'ına ekler.
 */
public class LoginResponseDto {

    private String token;
    private String name;
    private Role role;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, String name, Role role) {
        this.token = token;
        this.name = name;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
