package com.alper.nailbar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Bir personelin (STAFF) haftalık çalışma saatleri.
 * Her kayıt bir gün + çalışma saatini temsil eder.
 * isOffDay = true ise o gün personel izinlidir; startTime/endTime null olabilir.
 */
@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Personel boş olamaz.")
    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @NotNull(message = "Haftanın günü boş olamaz.")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    // isOffDay = true ise bu alanlar null olabilir
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_off_day", nullable = false)
    private boolean isOffDay = false;

    // --- Constructors ---

    public Shift() {
    }

    public Shift(User staff, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, boolean isOffDay) {
        this.staff = staff;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOffDay = isOffDay;
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public boolean isOffDay() {
        return isOffDay;
    }

    public void setOffDay(boolean offDay) {
        isOffDay = offDay;
    }
}
