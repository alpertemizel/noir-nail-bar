package com.alper.nailbar.controller;

import com.alper.nailbar.dto.CustomerNoteRequestDto;
import com.alper.nailbar.dto.CustomerNoteResponseDto;
import com.alper.nailbar.service.CustomerNoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Müşteri notu CRUD endpoint'leri.
 * Tümü STAFF veya SUPER_ADMIN yetkisi gerektirir (SecurityConfig'de /api/customer-notes/** korumalı).
 */
@RestController
@RequestMapping("/api/customer-notes")
public class CustomerNoteController {

    private final CustomerNoteService customerNoteService;

    public CustomerNoteController(CustomerNoteService customerNoteService) {
        this.customerNoteService = customerNoteService;
    }

    // Tüm müşteri notlarını listele
    // GET /api/customer-notes
    @GetMapping
    public ResponseEntity<List<CustomerNoteResponseDto>> getAll() {
        return ResponseEntity.ok(customerNoteService.getAllNotes());
    }

    // Telefon numarasına göre müşteri notlarını getir
    // GET /api/customer-notes/search?phone=05551234567
    @GetMapping("/search")
    public ResponseEntity<List<CustomerNoteResponseDto>> searchByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(customerNoteService.getNotesByPhone(phone));
    }

    // Yeni müşteri notu oluştur
    // POST /api/customer-notes
    @PostMapping
    public ResponseEntity<CustomerNoteResponseDto> create(
            @Valid @RequestBody CustomerNoteRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerNoteService.createNote(request));
    }

    // Mevcut notu güncelle
    // PUT /api/customer-notes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CustomerNoteResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerNoteRequestDto request) {
        return ResponseEntity.ok(customerNoteService.updateNote(id, request));
    }

    // Notu sil (sadece SUPER_ADMIN yapabilmeli — @PreAuthorize ile kısıtlandı)
    // DELETE /api/customer-notes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerNoteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
