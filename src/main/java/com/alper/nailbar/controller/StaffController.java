package com.alper.nailbar.controller;

import com.alper.nailbar.dto.AppointmentResponseDto;
import com.alper.nailbar.dto.ShiftResponseDto;
import com.alper.nailbar.dto.UserResponseDto;
import com.alper.nailbar.exception.ResourceNotFoundException;
import com.alper.nailbar.model.User;
import com.alper.nailbar.repository.UserRepository;
import com.alper.nailbar.service.AppointmentService;
import com.alper.nailbar.service.ShiftService;
import com.alper.nailbar.model.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Personel (STAFF) paneli endpoint'leri.
 * STAFF kendi randevularını ve vardiyalarını görebilir; ayrıca profile bilgisine erişebilir.
 * SUPER_ADMIN bu endpoint'lere de erişebilir (SecurityConfig'de hasAnyRole).
 *
 * Endpoint'ler:
 *   GET /api/staff/public         → Herkese açık personel listesi (randevu seçimi için)
 *   GET /api/staff/me             → Kendi profil bilgisi
 *   GET /api/staff/me/shifts      → Kendi vardiyaları
 *   GET /api/staff/me/appointments → Kendi randevuları (opsiyonel tarih filtresi)
 */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private static final Logger log = LoggerFactory.getLogger(StaffController.class);

    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final ShiftService shiftService;

    public StaffController(UserRepository userRepository,
                           AppointmentService appointmentService,
                           ShiftService shiftService) {
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
        this.shiftService = shiftService;
    }

    /**
     * GET /api/staff/public
     * Müşterilerin randevu alırken personel seçebilmesi için herkese açık personel listesi.
     */
    @GetMapping("/public")
    public ResponseEntity<List<UserResponseDto>> getPublicStaffList() {
        // Önce aktif STAFF'ları çek; boş gelirse tüm STAFF'ları dene (active alanı sorunlu olabilir)
        List<User> activeStaff = userRepository.findAllByRoleAndActiveTrue(Role.STAFF);
        log.info("[/api/staff/public] Aktif STAFF sayısı: {}", activeStaff.size());

        if (activeStaff.isEmpty()) {
            // active alanı null veya yanlış kaydedilmiş olabilir — tüm STAFF'ları getir
            List<User> allStaff = userRepository.findAllByRole(Role.STAFF);
            log.warn("[/api/staff/public] Aktif STAFF bulunamadı. Tüm STAFF sayısı: {}. active alanlarını kontrol edin.", allStaff.size());
            activeStaff = allStaff;
        }

        List<UserResponseDto> staffList = activeStaff.stream()
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole(),
                        user.getActive()
                ))
                .toList();
        return ResponseEntity.ok(staffList);
    }

    /**
     * GET /api/staff/me
     * Token sahibinin kendi profil bilgilerini döner (şifre hariç).
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        ));
    }

    /**
     * GET /api/staff/me/shifts
     * Token sahibinin haftalık vardiya planını listeler.
     */
    @GetMapping("/me/shifts")
    public ResponseEntity<List<ShiftResponseDto>> getMyShifts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(shiftService.getShiftsByStaff(user.getId()));
    }

    /**
     * GET /api/staff/me/appointments?date=2026-08-10
     * Token sahibine atanmış randevuları listeler. Tarih filtresi opsiyonel.
     */
    @GetMapping("/me/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(appointmentService.getAppointmentsByStaff(user.getId(), date));
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    /**
     * JWT'den alınan email ile DB'den tam User objesini çeker.
     * UserDetails.username = email olarak ayarlandığı için bu dönüşüm güvenlidir.
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oturum bilgisi geçersiz. Lütfen tekrar giriş yapın."));
    }
}
