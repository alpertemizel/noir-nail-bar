package com.alper.nailbar.dto.auth;

import com.alper.nailbar.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Yeni personel (STAFF) kaydı için istek şablonu.
 * Bu endpoint'i sadece SUPER_ADMIN çağırabilir (SecurityConfig'de korumalı).
 */
public class RegisterRequestDto {

    @NotBlank(message = "Ad boş olamaz.")
    private String name;

    @NotBlank(message = "E-posta boş olamaz.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @NotBlank(message = "Şifre boş olamaz.")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır.")
    private String password;

    @Pattern(regexp = "^05\\d{9}$", message = "Telefon numarası '05XXXXXXXXX' formatında olmalıdır.")
    private String phone;

    @NotNull(message = "Rol boş olamaz.")
    private Role role;

    public RegisterRequestDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
