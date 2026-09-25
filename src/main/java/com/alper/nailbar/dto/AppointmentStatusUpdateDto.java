package com.alper.nailbar.dto;

import com.alper.nailbar.model.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Randevu durumunu güncellemek için kullanılan istek şablonu.
 * Örnek: {"status": "APPROVED"}
 */
public class AppointmentStatusUpdateDto {

    @NotNull(message = "Durum boş olamaz.")
    private AppointmentStatus status;

    public AppointmentStatusUpdateDto() {
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
