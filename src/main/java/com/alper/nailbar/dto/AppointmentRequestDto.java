package com.alper.nailbar.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

// Müşterinin randevu oluşturmak için gönderdiği veri şablonu
public class AppointmentRequestDto {

    @NotBlank(message = "Müşteri adı boş olamaz.")
    private String customerName;

    @NotBlank(message = "Telefon numarası boş olamaz.")
    @Pattern(regexp = "^05\\d{9}$", message = "Telefon numarası '05XXXXXXXXX' formatında olmalıdır.")
    private String customerPhone;

    @NotNull(message = "Hizmet seçimi zorunludur.")
    private Long serviceId; // Artık iç içe JSON objesi değil, sadece ID yeterli!

    @NotNull(message = "Randevu tarihi ve saati boş olamaz.")
    @Future(message = "Randevu tarihi gelecekte bir zaman olmalıdır.")
    private LocalDateTime appointmentTime;

    private Long staffId; // Opsiyonel personel seçimi

    // Boş Constructor (Jackson için gerekli)
    public AppointmentRequestDto() {
    }

    // Getter ve Setter Metotları
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
}
