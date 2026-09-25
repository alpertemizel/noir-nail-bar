package com.alper.nailbar.service;

import com.alper.nailbar.dto.ShiftRequestDto;
import com.alper.nailbar.dto.ShiftResponseDto;
import com.alper.nailbar.exception.ResourceNotFoundException;
import com.alper.nailbar.model.Shift;
import com.alper.nailbar.model.User;
import com.alper.nailbar.repository.ShiftRepository;
import com.alper.nailbar.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public ShiftService(ShiftRepository shiftRepository, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    // --- Tüm vardiyaları listele (admin için) ---
    public List<ShiftResponseDto> getAllShifts() {
        return shiftRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // --- Belirli personelin vardiyaları ---
    public List<ShiftResponseDto> getShiftsByStaff(Long staffId) {
        // Personelin varlığını doğrula
        if (!userRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Personel bulunamadı: ID=" + staffId);
        }
        return shiftRepository.findByStaffId(staffId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // --- Yeni vardiya oluştur ---
    public ShiftResponseDto createShift(ShiftRequestDto request) {
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personel bulunamadı: ID=" + request.getStaffId()));

        Shift shift = new Shift(
                staff,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.isOffDay()
        );
        return toResponseDto(shiftRepository.save(shift));
    }

    // --- Vardiya güncelle ---
    public ShiftResponseDto updateShift(Long id, ShiftRequestDto request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vardiya bulunamadı: ID=" + id));

        // Personel değişebilir — örn: yanlış personele atanmış vardiya düzeltilir
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personel bulunamadı: ID=" + request.getStaffId()));

        shift.setStaff(staff);
        shift.setDayOfWeek(request.getDayOfWeek());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setOffDay(request.isOffDay());

        return toResponseDto(shiftRepository.save(shift));
    }

    // --- Vardiya sil ---
    public void deleteShift(Long id) {
        if (!shiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vardiya bulunamadı: ID=" + id);
        }
        shiftRepository.deleteById(id);
    }

    // --- Belirli personelin tüm vardiyalarını sil ---
    public void deleteShiftsByStaff(Long staffId) {
        List<Shift> shifts = shiftRepository.findByStaffId(staffId);
        shiftRepository.deleteAll(shifts);
    }

    // --- Yardımcı: Entity → DTO ---
    private ShiftResponseDto toResponseDto(Shift shift) {
        return new ShiftResponseDto(
                shift.getId(),
                shift.getStaff().getId(),
                shift.getStaff().getName(),
                shift.getDayOfWeek(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.isOffDay()
        );
    }
}
