package com.alper.nailbar.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AppointmentRescheduleDto(
        @NotNull(message = "Yeni randevu tarihi boş olamaz")
        LocalDateTime newAppointmentTime
) {}
