package com.alper.nailbar.service;

import com.alper.nailbar.dto.AppointmentRequestDto;
import com.alper.nailbar.dto.AppointmentResponseDto;
import com.alper.nailbar.dto.AvailableSlotDto;
import com.alper.nailbar.dto.MonthlyAnalyticsDto;
import com.alper.nailbar.exception.AppointmentConflictException;
import com.alper.nailbar.exception.ResourceNotFoundException;
import com.alper.nailbar.model.Appointment;
import com.alper.nailbar.model.NailService;
import com.alper.nailbar.model.User;
import com.alper.nailbar.model.enums.AppointmentStatus;
import com.alper.nailbar.repository.AppointmentRepository;
import com.alper.nailbar.repository.NailServiceRepository;
import com.alper.nailbar.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final NailServiceRepository nailServiceRepository;
    private final UserRepository userRepository;

    // Dükkanın Sabit Çalışma Saatleri
    private static final LocalTime WORK_START = LocalTime.of(10, 0);
    private static final LocalTime WORK_END = LocalTime.of(20, 0);

    // Masa temizleme/sterilizasyon süresi (dakika)
    private static final int CLEANUP_MINUTES = 15;

    // Takvimde gösterilecek saat dilimi aralığı (30 dakikada bir kontrol edilir)
    private static final int SLOT_INTERVAL_MINUTES = 30;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              NailServiceRepository nailServiceRepository,
                              UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.nailServiceRepository = nailServiceRepository;
        this.userRepository = userRepository;
    }

    // --- Randevu Oluşturma ---
    public AppointmentResponseDto createAppointment(AppointmentRequestDto requestDto) {
        // Hizmetin veritabanında var olduğunu doğrula (yoksa 404)
        NailService dbService = nailServiceRepository.findById(requestDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ID'si " + requestDto.getServiceId() + " olan hizmet sistemde bulunamadı!"));

        User staff = null;
        if (requestDto.getStaffId() != null) {
            staff = userRepository.findById(requestDto.getStaffId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ID'si " + requestDto.getStaffId() + " olan personel bulunamadı!"));
        }

        LocalTime appTime = requestDto.getAppointmentTime().toLocalTime();
        int totalMinutes = dbService.getDurationInMinutes() + CLEANUP_MINUTES;
        LocalTime appEndTime = appTime.plusMinutes(totalMinutes);

        // KURAL 1: Çalışma saatleri kontrolü
        if (appTime.isBefore(WORK_START) || appEndTime.isAfter(WORK_END)) {
            throw new AppointmentConflictException(
                    "İstediğiniz işlem dükkanın çalışma saatlerine (10:00 - 20:00) sığmıyor!");
        }

        // KURAL 2: Çakışma kontrolü (sadece o güne ait randevular sorgulanır)
        LocalDateTime startOfDay = requestDto.getAppointmentTime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = requestDto.getAppointmentTime().toLocalDate().atTime(LocalTime.MAX);
        List<Appointment> existingAppointments = appointmentRepository
                .findAllByAppointmentTimeBetween(startOfDay, endOfDay);

        for (Appointment existingApp : existingAppointments) {
            // Randevu iptal edildiyse çakışma sayma
            if (existingApp.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }

            LocalTime existingStart = existingApp.getAppointmentTime().toLocalTime();
            int existingTotal = existingApp.getNailService().getDurationInMinutes() + CLEANUP_MINUTES;
            LocalTime existingEnd = existingStart.plusMinutes(existingTotal);

            if (appTime.isBefore(existingEnd) && appEndTime.isAfter(existingStart)) {
                if (staff != null) {
                    // Personel seçilmişse ve o personelin çakışan randevusu varsa
                    if (existingApp.getStaff() != null && existingApp.getStaff().getId().equals(staff.getId())) {
                        throw new AppointmentConflictException(
                                "Seçtiğiniz uzmanın (" + staff.getName() + ") bu saat diliminde başka bir randevusu bulunmaktadır!");
                    }
                } else {
                    // Personel seçilmemişse dükkan geneli çakışma uyarısı
                    throw new AppointmentConflictException(
                            "Seçtiğiniz saat aralığında salonumuz doludur!");
                }
            }
        }

        // Veritabanına kaydet
        Appointment newApp = new Appointment(
                null,
                requestDto.getCustomerName(),
                requestDto.getCustomerPhone(),
                dbService,
                requestDto.getAppointmentTime()
        );
        newApp.setStaff(staff);
        Appointment saved = appointmentRepository.save(newApp);

        // Temiz DTO olarak döndür
        return toResponseDto(saved);
    }

    // --- Tüm Randevuları Listele ---
    public List<AppointmentResponseDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // --- Dinamik Boş Saat Hesaplama ---
    public List<AvailableSlotDto> getAvailableSlots(LocalDate date, Long serviceId, Integer totalDurationOverride) {
        return getAvailableSlots(date, serviceId, totalDurationOverride, null);
    }

    public List<AvailableSlotDto> getAvailableSlots(LocalDate date, Long serviceId, Integer totalDurationOverride, Long staffId) {
        // Hizmetin var olduğunu doğrula
        NailService service = nailServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ID'si " + serviceId + " olan hizmet sistemde bulunamadı!"));

        int serviceDuration = (totalDurationOverride != null && totalDurationOverride > 0)
                ? totalDurationOverride
                : service.getDurationInMinutes();
        int totalMinutes = serviceDuration + CLEANUP_MINUTES;

        // O güne ait mevcut randevuları veritabanından çek
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> existingAppointments = appointmentRepository
                .findAllByAppointmentTimeBetween(startOfDay, endOfDay);

        List<AvailableSlotDto> availableSlots = new ArrayList<>();

        // 10:00'dan 20:00'a kadar 30'ar dakikalık adımlarla tüm olası başlangıç saatlerini kontrol et
        LocalTime cursor = WORK_START;
        while (!cursor.plusMinutes(totalMinutes).isAfter(WORK_END)) {
            LocalTime candidateEnd = cursor.plusMinutes(totalMinutes);
            boolean isConflict = false;

            for (Appointment existing : existingAppointments) {
                if (existing.getStatus() == AppointmentStatus.CANCELLED) {
                    continue;
                }

                LocalTime existingStart = existing.getAppointmentTime().toLocalTime();
                int existingTotal = existing.getNailService().getDurationInMinutes() + CLEANUP_MINUTES;
                LocalTime existingEnd = existingStart.plusMinutes(existingTotal);

                if (cursor.isBefore(existingEnd) && candidateEnd.isAfter(existingStart)) {
                    if (staffId != null) {
                        // Belirli bir personel seçilmişse sadece o personelin randevularıyla çakışma bak
                        if (existing.getStaff() != null && existing.getStaff().getId().equals(staffId)) {
                            isConflict = true;
                            break;
                        }
                    } else {
                        // Personel seçilmemişse herhangi bir randevuda çakışma var mı bak
                        isConflict = true;
                        break;
                    }
                }
            }

            if (!isConflict) {
                availableSlots.add(new AvailableSlotDto(cursor, cursor.plusMinutes(serviceDuration)));
            }

            cursor = cursor.plusMinutes(SLOT_INTERVAL_MINUTES);
        }

        return availableSlots;
    }

    // --- Randevuya Personel Atama / Güncelleme ---
    public AppointmentResponseDto assignStaff(Long appointmentId, Long staffId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: ID=" + appointmentId));

        if (staffId != null) {
            User staff = userRepository.findById(staffId)
                    .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı: ID=" + staffId));
            appointment.setStaff(staff);
        } else {
            appointment.setStaff(null);
        }

        return toResponseDto(appointmentRepository.save(appointment));
    }

    // --- Randevu Durumu Güncelleme ---
    public AppointmentResponseDto updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Randevu bulunamadı: ID=" + id));

        if (newStatus == AppointmentStatus.COMPLETED) {
            if (appointment.getAppointmentTime() != null && appointment.getAppointmentTime().isAfter(LocalDateTime.now())) {
                throw new IllegalStateException("Randevu saati gelmeden işlem tamamlandı olarak işaretlenemez.");
            }
        }

        appointment.setStatus(newStatus);
        return toResponseDto(appointmentRepository.save(appointment));
    }

    // --- Randevu Erteleme / Tarih-Saat Güncelleme ---
    public AppointmentResponseDto rescheduleAppointment(Long id, LocalDateTime newAppointmentTime) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: ID=" + id));

        appointment.setAppointmentTime(newAppointmentTime);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }
        return toResponseDto(appointmentRepository.save(appointment));
    }

    // --- Randevu Silme (İptal) ---
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Randevu bulunamadı: ID=" + id);
        }
        appointmentRepository.deleteById(id);
    }

    // --- Tarihe Göre Filtrelenmiş Randevular (Admin Panel) ---
    // date null ise sadece mevcut ayın randevuları tarihe/saate göre sıralı döner
    public List<AppointmentResponseDto> getAllAppointmentsFiltered(LocalDate date) {
        if (date == null) {
            LocalDate now = LocalDate.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
            return appointmentRepository.findAllByAppointmentTimeBetweenOrderByAppointmentTimeAsc(startOfMonth, endOfMonth)
                    .stream().map(this::toResponseDto).toList();
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return appointmentRepository.findAllByAppointmentTimeBetweenOrderByAppointmentTimeAsc(start, end)
                .stream().map(this::toResponseDto).toList();
    }

    // --- Personele Ait Randevular (Staff Panel) ---
    // date null ise sadece mevcut ayın randevuları tarihe ve saate göre sıralı döner
    public List<AppointmentResponseDto> getAppointmentsByStaff(Long staffId, LocalDate date) {
        if (date == null) {
            LocalDate now = LocalDate.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
            return appointmentRepository
                    .findAllByStaffIdAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(staffId, startOfMonth, endOfMonth)
                    .stream().map(this::toResponseDto).toList();
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return appointmentRepository
                .findAllByStaffIdAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(staffId, start, end)
                .stream().map(this::toResponseDto).toList();
    }

    // --- Aylık İş Analizi ---
    public MonthlyAnalyticsDto getMonthlyAnalytics(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

        // Ay içindeki tüm randevuları çek
        List<Appointment> allInMonth = appointmentRepository
                .findAllByAppointmentTimeBetweenOrderByAppointmentTimeAsc(start, end);

        long totalAppointments = allInMonth.size();

        // Tamamlanan randevular
        List<Appointment> completed = allInMonth.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();

        long completedCount = completed.size();

        // İptal edilen randevular
        long cancelledCount = allInMonth.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .count();

        // Toplam gelir (sadece tamamlanan randevulardan)
        double totalRevenue = completed.stream()
                .mapToDouble(a -> a.getNailService() != null ? a.getNailService().getPrice() : 0.0)
                .sum();

        double avgRevenue = completedCount > 0 ? totalRevenue / completedCount : 0.0;

        // Personel bazlı dağılım
        Map<String, List<Appointment>> byStaff = completed.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStaff() != null ? a.getStaff().getName() : "Atanmamış",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<MonthlyAnalyticsDto.StaffRevenueDto> staffBreakdown = byStaff.entrySet().stream()
                .map(e -> new MonthlyAnalyticsDto.StaffRevenueDto(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .mapToDouble(a -> a.getNailService() != null ? a.getNailService().getPrice() : 0.0)
                                .sum()
                ))
                .sorted((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()))
                .toList();

        // Hizmet bazlı dağılım
        Map<String, List<Appointment>> byService = completed.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getNailService() != null ? a.getNailService().getName() : "Bilinmeyen",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<MonthlyAnalyticsDto.ServiceRevenueDto> serviceBreakdown = byService.entrySet().stream()
                .map(e -> new MonthlyAnalyticsDto.ServiceRevenueDto(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .mapToDouble(a -> a.getNailService() != null ? a.getNailService().getPrice() : 0.0)
                                .sum()
                ))
                .sorted((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()))
                .toList();

        // DTO'yu oluştur
        MonthlyAnalyticsDto dto = new MonthlyAnalyticsDto();
        dto.setYear(year);
        dto.setMonth(month);
        dto.setTotalAppointments(totalAppointments);
        dto.setCompletedAppointments(completedCount);
        dto.setCancelledAppointments(cancelledCount);
        dto.setTotalRevenue(totalRevenue);
        dto.setAverageRevenuePerAppointment(avgRevenue);
        dto.setStaffBreakdown(staffBreakdown);
        dto.setServiceBreakdown(serviceBreakdown);

        return dto;
    }

    // --- Yardımcı Metot: Entity → DTO Dönüşümü ---
    private AppointmentResponseDto toResponseDto(Appointment app) {
        // Bitiş saati = hizmet süresi + temizlik süresi
        // Frontend çoklu randevuda bu saati bir sonraki randevunun başlangıcı olarak kullanır.
        // Backend çakışma kontrolü de temizlik süresini dahil ettiği için tutarlı olması şarttır.
        LocalDateTime endTime = app.getAppointmentTime()
                .plusMinutes(app.getNailService().getDurationInMinutes() + CLEANUP_MINUTES);

        String staffName = (app.getStaff() != null) ? app.getStaff().getName() : null;

        return new AppointmentResponseDto(
                app.getId(),
                app.getCustomerName(),
                app.getCustomerPhone(),
                app.getNailService().getName(),
                app.getNailService().getPrice(),
                app.getAppointmentTime(),
                endTime,
                app.getStatus(),
                staffName
        );
    }
}