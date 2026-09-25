package com.alper.nailbar.controller;

import com.alper.nailbar.model.NailService;
import com.alper.nailbar.service.NailServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class NailServiceController {

    private final NailServiceService nailServiceService;

    public NailServiceController(NailServiceService nailServiceService) {
        this.nailServiceService = nailServiceService;
    }

    // Tüm hizmetleri listele (GET /api/services)
    @GetMapping
    public ResponseEntity<List<NailService>> getAll() {
        return ResponseEntity.ok(nailServiceService.getAllServices());
    }

    // ID ile tek bir hizmet getir (GET /api/services/{id})
    @GetMapping("/{id}")
    public ResponseEntity<NailService> getById(@PathVariable Long id) {
        return ResponseEntity.ok(nailServiceService.getServiceById(id));
    }

    // Yeni hizmet ekle (POST /api/services)
    @PostMapping
    public ResponseEntity<NailService> create(@Valid @RequestBody NailService newService) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nailServiceService.createService(newService));
    }

    // Mevcut hizmeti güncelle (PUT /api/services/{id})
    @PutMapping("/{id}")
    public ResponseEntity<NailService> update(@PathVariable Long id, @Valid @RequestBody NailService updatedService) {
        return ResponseEntity.ok(nailServiceService.updateService(id, updatedService));
    }

    // Hizmet sil (DELETE /api/services/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        nailServiceService.deleteService(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}