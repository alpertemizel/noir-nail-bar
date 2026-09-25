package com.alper.nailbar.dto;

import java.time.LocalTime;

// Bir günün takviminde tek bir boş saat dilimini temsil eden veri şablonu
public class AvailableSlotDto {

    private LocalTime startTime;  // Randevunun başlayabileceği saat (örn: 10:00)
    private LocalTime endTime;    // Randevunun biteceği saat (temizlik süresi dahil değil!)

    public AvailableSlotDto(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
