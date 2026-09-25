package com.alper.nailbar.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Vardiya (çalışma saati) oluşturma veya güncelleme isteği.
 * SUPER_ADMIN tarafından gönderilir.
 */
public class ShiftRequestDto {

    @NotNull(message = "Personel ID boş olamaz.")
    private Long staffId;

    @NotNull(message = "Gün boş olamaz.")
    private DayOfWeek dayOfWeek;

    // isOffDay = true ise bu alanlar null olabilir
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isOffDay = false;

    public ShiftRequestDto() {
    }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isOffDay() { return isOffDay; }
    public void setOffDay(boolean offDay) { isOffDay = offDay; }
}
