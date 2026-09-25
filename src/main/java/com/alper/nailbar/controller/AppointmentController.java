package com.alper.nailbar.controller;

import com.alper.nailbar.dto.AppointmentRequestDto;
import com.alper.nailbar.dto.AppointmentResponseDto;
import com.alper.nailbar.dto.AppointmentStatusUpdateDto;
import com.alper.nailbar.dto.AvailableSlotDto;
import com.alper.nailbar.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Kayıtlı tüm randevuları listeler — STAFF/SUPER_ADMIN (SecurityConfig'de korumalı)
    // Opsiyonel date filtresi: GET /api/appointments?date=2026-08-10
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDto>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsFiltered(date));
    }

    // Yeni randevu oluşturur (POST /api/appointments) — herkese açık
    // @Valid anotasyonu, DTO içindeki @NotBlank, @Future gibi kuralları otomatik tetikler
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(@Valid @RequestBody AppointmentRequestDto requestDto) {
        AppointmentResponseDto created = appointmentService.createAppointment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Seçili tarih ve hizmet için boş saatleri listeler — herkese açık
    // Örnek istek: GET /api/appointments/available-slots?date=2026-07-28&serviceId=1&totalDurationMinutes=120&staffId=2
    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotDto>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId,
            @RequestParam(required = false) Integer totalDurationMinutes,
            @RequestParam(required = false) Long staffId) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(date, serviceId, totalDurationMinutes, staffId));
    }

    // Randevuya personel atar/günceller — STAFF/SUPER_ADMIN (SecurityConfig'de korumalı)
    // Örnek: PUT /api/appointments/5/staff?staffId=2  (staffId opsiyonel, null gönderilirse personelsiz yapılır)
    @PutMapping("/{id}/staff")
    public ResponseEntity<AppointmentResponseDto> assignStaff(
            @PathVariable Long id,
            @RequestParam(required = false) Long staffId) {
        return ResponseEntity.ok(appointmentService.assignStaff(id, staffId));
    }

    // Randevu durumunu günceller — STAFF/SUPER_ADMIN (SecurityConfig'de korumalı)
    // Örnek: PUT /api/appointments/5/status  body: {"status": "APPROVED"}
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateDto statusDto) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, statusDto.getStatus()));
    }

    // Randevu erteler / saatini günceller — STAFF/SUPER_ADMIN (SecurityConfig'de korumalı)
    // Örnek: PUT /api/appointments/5/reschedule  body: {"newAppointmentTime": "2026-08-10T14:30:00"}
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDto> reschedule(
            @PathVariable Long id,
            @Valid @RequestBody com.alper.nailbar.dto.AppointmentRescheduleDto rescheduleDto) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, rescheduleDto.newAppointmentTime()));
    }

    // Randevu sil / iptal et — SUPER_ADMIN veya STAFF (SecurityConfig'de korumalı)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}