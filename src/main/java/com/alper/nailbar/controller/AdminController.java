package com.alper.nailbar.controller;

import com.alper.nailbar.dto.*;
import com.alper.nailbar.model.Appointment;
import com.alper.nailbar.model.User;
import com.alper.nailbar.model.enums.AppointmentStatus;
import com.alper.nailbar.model.enums.Role;
import com.alper.nailbar.repository.AppointmentRepository;
import com.alper.nailbar.repository.UserRepository;
import com.alper.nailbar.service.AppointmentService;
import com.alper.nailbar.service.ShiftService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Admin paneli endpoint'leri — tümü SUPER_ADMIN yetkisi gerektirir (SecurityConfig + @PreAuthorize).
 *
 * Endpoint'ler:
 *   GET    /api/admin/dashboard           → İstatistik özeti
 *   GET    /api/admin/users               → Tüm kullanıcılar
 *   GET    /api/admin/users/staff         → Sadece STAFF rolündeki kullanıcılar
 *   DELETE /api/admin/users/{id}          → Kullanıcı sil
 *   GET    /api/admin/appointments        → Tüm randevular (opsiyonel tarih filtresi)
 *   GET    /api/admin/shifts              → Tüm vardiyalar
 *   GET    /api/admin/shifts/staff/{id}   → Belirli personelin vardiyaları
 *   POST   /api/admin/shifts              → Vardiya oluştur
 *   PUT    /api/admin/shifts/{id}         → Vardiya güncelle
 *   DELETE /api/admin/shifts/{id}         → Vardiya sil
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")  // Tüm controller sadece SUPER_ADMIN
public class AdminController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final ShiftService shiftService;

    public AdminController(UserRepository userRepository,
                           AppointmentRepository appointmentRepository,
                           AppointmentService appointmentService,
                           ShiftService shiftService) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        this.shiftService = shiftService;
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/dashboard
     * Bugünkü randevular, bekleyen randevular, toplam personel, bugün tamamlanan işlemler.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboard() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        long todayCount = appointmentRepository.countByAppointmentTimeBetween(startOfToday, endOfToday);
        long pendingCount = appointmentRepository.countByStatus(AppointmentStatus.PENDING);
        long staffCount = userRepository.countByRoleAndActiveTrue(Role.STAFF);
        long completedToday = appointmentRepository.countByStatusAndAppointmentTimeBetween(
                AppointmentStatus.COMPLETED, startOfToday, endOfToday);

        return ResponseEntity.ok(new DashboardStatsDto(todayCount, pendingCount, staffCount, completedToday));
    }

    /**
     * GET /api/admin/analytics?year=2026&month=8
     * Aylık iş analizi: tamamlanan randevu sayısı, gelir, personel ve hizmet bazlı dağılım.
     */
    @GetMapping("/analytics")
    public ResponseEntity<MonthlyAnalyticsDto> getMonthlyAnalytics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();
        return ResponseEntity.ok(appointmentService.getMonthlyAnalytics(y, m));
    }

    // ─── Kullanıcı Yönetimi ────────────────────────────────────────────────

    /**
     * GET /api/admin/users — Sistemdeki tüm kullanıcılar (şifre hariç)
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userRepository.findAll()
                .stream()
                .map(this::toUserDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/admin/users/staff — Sadece STAFF rolündeki personeller
     */
    @GetMapping("/users/staff")
    public ResponseEntity<List<UserResponseDto>> getStaffUsers() {
        List<UserResponseDto> staff = userRepository.findAllByRole(Role.STAFF)
                .stream()
                .map(this::toUserDto)
                .toList();
        return ResponseEntity.ok(staff);
    }

    /**
     * PUT /api/admin/users/{id}/toggle-active — Personel aktiflik durumunu değiştir (Aktif / Pasif)
     * Eğer personel pasife alındıysa, ona ait onaylanan (APPROVED) randevular otomatik olarak Bekliyor (PENDING) durumuna geçer.
     */
    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserResponseDto> toggleUserActive(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı: ID=" + id));
        boolean currentActive = user.getActive() == null || Boolean.TRUE.equals(user.getActive());
        boolean newActive = !currentActive;
        user.setActive(newActive);
        User saved = userRepository.save(user);

        // Personel pasife alındığında, ona ait onaylanan (APPROVED) randevular Bekliyor (PENDING) durumuna geçer
        if (!newActive) {
            List<Appointment> staffAppointments = appointmentRepository.findAllByStaffId(id);
            for (Appointment app : staffAppointments) {
                if (app.getStatus() == AppointmentStatus.APPROVED) {
                    app.setStatus(AppointmentStatus.PENDING);
                    appointmentRepository.save(app);
                }
            }
        }

        return ResponseEntity.ok(toUserDto(saved));
    }

    /**
     * DELETE /api/admin/users/{id} — Kullanıcıyı kalıcı olarak sil
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı: ID=" + id);
        }
        // İlişkili randevulardaki personel atamasını kaldır ve onaylıları beklemeye al
        List<Appointment> appointments = appointmentRepository.findAllByStaffId(id);
        for (Appointment app : appointments) {
            app.setStaff(null);
            if (app.getStatus() == AppointmentStatus.APPROVED) {
                app.setStatus(AppointmentStatus.PENDING);
            }
            appointmentRepository.save(app);
        }
        // İlişkili vardiyaları temizle
        shiftService.deleteShiftsByStaff(id);

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Randevu Yönetimi ────────────────────────────────────────────────────

    /**
     * GET /api/admin/appointments?date=2026-08-10
     * Tüm randevular, opsiyonel olarak tarih filtrelenebilir
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsFiltered(date));
    }

    // ─── Vardiya Yönetimi ─────────────────────────────────────────────────────

    /**
     * GET /api/admin/shifts — Tüm personellerin tüm vardiyaları
     */
    @GetMapping("/shifts")
    public ResponseEntity<List<ShiftResponseDto>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    /**
     * GET /api/admin/shifts/staff/{staffId} — Belirli bir personelin vardiyaları
     */
    @GetMapping("/shifts/staff/{staffId}")
    public ResponseEntity<List<ShiftResponseDto>> getShiftsForStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(shiftService.getShiftsByStaff(staffId));
    }

    /**
     * POST /api/admin/shifts — Yeni vardiya oluştur
     */
    @PostMapping("/shifts")
    public ResponseEntity<ShiftResponseDto> createShift(@Valid @RequestBody ShiftRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.createShift(request));
    }

    /**
     * PUT /api/admin/shifts/{id} — Vardiya güncelle
     */
    @PutMapping("/shifts/{id}")
    public ResponseEntity<ShiftResponseDto> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequestDto request) {
        return ResponseEntity.ok(shiftService.updateShift(id, request));
    }

    /**
     * DELETE /api/admin/shifts/{id} — Vardiya sil
     */
    @DeleteMapping("/shifts/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Yardımcı ────────────────────────────────────────────────────────────

    private UserResponseDto toUserDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getActive() == null || Boolean.TRUE.equals(user.getActive())
        );
    }
}
