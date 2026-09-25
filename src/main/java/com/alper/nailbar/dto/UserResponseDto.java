package com.alper.nailbar.dto;

import com.alper.nailbar.model.enums.Role;

/**
 * Kullanıcı bilgilerini frontend'e dönerken kullanılan DTO.
 * Şifre (password) hiçbir zaman bu DTO'ya dahil edilmez!
 */
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Boolean active = true;

    public UserResponseDto() {
        this.active = true;
    }

    public UserResponseDto(Long id, String name, String email, String phone, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = true;
    }

    public UserResponseDto(Long id, String name, String email, String phone, Role role, Boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active != null ? active : true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getActive() { return active != null ? active : true; }
    public void setActive(Boolean active) { this.active = active != null ? active : true; }
}
