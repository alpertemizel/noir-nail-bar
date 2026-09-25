package com.alper.nailbar.repository;

import com.alper.nailbar.model.Appointment;
import com.alper.nailbar.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Tarihe ve saate göre sıralı randevu sorguları
    List<Appointment> findAllByOrderByAppointmentTimeAsc();

    List<Appointment> findAllByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findAllByAppointmentTimeBetweenOrderByAppointmentTimeAsc(
            LocalDateTime start, LocalDateTime end);

    List<Appointment> findAllByStaffId(Long staffId);

    List<Appointment> findAllByStaffIdOrderByAppointmentTimeAsc(Long staffId);

    List<Appointment> findAllByStaffIdAndAppointmentTimeBetween(
            Long staffId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findAllByStaffIdAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(
            Long staffId, LocalDateTime start, LocalDateTime end);

    // Belirli durumdaki randevular (örn: tüm PENDING randevular)
    List<Appointment> findAllByStatus(AppointmentStatus status);

    // Dashboard istatistikleri için sayım metodları
    long countByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(AppointmentStatus status);
    long countByStatusAndAppointmentTimeBetween(
            AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    // Aylık analiz için durum + tarih aralığı sorgusu
    List<Appointment> findAllByStatusAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(
            AppointmentStatus status, LocalDateTime start, LocalDateTime end);
}