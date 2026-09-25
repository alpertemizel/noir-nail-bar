package com.alper.nailbar.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ShiftResponseDto {

    private Long id;
    private Long staffId;
    private String staffName;    // Entity FK yerine okunabilir isim
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isOffDay;

    public ShiftResponseDto() {
    }

    public ShiftResponseDto(Long id, Long staffId, String staffName,
                            DayOfWeek dayOfWeek, LocalTime startTime,
                            LocalTime endTime, boolean isOffDay) {
        this.id = id;
        this.staffId = staffId;
        this.staffName = staffName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOffDay = isOffDay;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isOffDay() { return isOffDay; }
    public void setOffDay(boolean offDay) { isOffDay = offDay; }
}
