package com.alper.nailbar.service;

import com.alper.nailbar.dto.CustomerNoteRequestDto;
import com.alper.nailbar.dto.CustomerNoteResponseDto;
import com.alper.nailbar.exception.ResourceNotFoundException;
import com.alper.nailbar.model.CustomerNote;
import com.alper.nailbar.repository.CustomerNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerNoteService {

    private final CustomerNoteRepository customerNoteRepository;

    public CustomerNoteService(CustomerNoteRepository customerNoteRepository) {
        this.customerNoteRepository = customerNoteRepository;
    }

    // --- Tüm notları listele ---
    public List<CustomerNoteResponseDto> getAllNotes() {
        return customerNoteRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // --- Telefon numarasına göre müşteri notlarını getir ---
    public List<CustomerNoteResponseDto> getNotesByPhone(String phone) {
        return customerNoteRepository.findByCustomerPhone(phone)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // --- Yeni not oluştur ---
    public CustomerNoteResponseDto createNote(CustomerNoteRequestDto request) {
        CustomerNote note = new CustomerNote(
                request.getCustomerName(),
                request.getCustomerPhone(),
                request.getNoteText()
        );
        return toResponseDto(customerNoteRepository.save(note));
    }

    // --- Mevcut notu güncelle ---
    public CustomerNoteResponseDto updateNote(Long id, CustomerNoteRequestDto request) {
        CustomerNote note = customerNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not bulunamadı: ID=" + id));

        note.setCustomerName(request.getCustomerName());
        note.setCustomerPhone(request.getCustomerPhone());
        note.setNoteText(request.getNoteText());

        return toResponseDto(customerNoteRepository.save(note));
    }

    // --- Not sil ---
    public void deleteNote(Long id) {
        if (!customerNoteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Not bulunamadı: ID=" + id);
        }
        customerNoteRepository.deleteById(id);
    }

    // --- Yardımcı: Entity → DTO ---
    private CustomerNoteResponseDto toResponseDto(CustomerNote note) {
        return new CustomerNoteResponseDto(
                note.getId(),
                note.getCustomerName(),
                note.getCustomerPhone(),
                note.getNoteText(),
                note.getCreatedAt()
        );
    }
}
