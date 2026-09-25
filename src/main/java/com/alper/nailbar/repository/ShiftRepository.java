package com.alper.nailbar.repository;

import com.alper.nailbar.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    // Belirli bir personelin tüm vardiyalarını getirir
    List<Shift> findByStaffId(Long staffId);

    // Belirli bir personelin belirli bir gündeki vardiyasını getirir
    Optional<Shift> findByStaffIdAndDayOfWeek(Long staffId, DayOfWeek dayOfWeek);
}
