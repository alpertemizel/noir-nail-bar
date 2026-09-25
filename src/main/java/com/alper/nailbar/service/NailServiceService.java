package com.alper.nailbar.service;

import com.alper.nailbar.exception.ResourceNotFoundException;
import com.alper.nailbar.model.NailService;
import com.alper.nailbar.repository.NailServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Hizmet (NailService) iş mantığı burada yönetilir
// Controller → Service → Repository katman zinciri bu sınıfla tamamlanıyor
@Service
public class NailServiceService {

    private final NailServiceRepository nailServiceRepository;

    public NailServiceService(NailServiceRepository nailServiceRepository) {
        this.nailServiceRepository = nailServiceRepository;
    }

    // Tüm hizmetleri getir
    public List<NailService> getAllServices() {
        return nailServiceRepository.findAll();
    }

    // ID ile tek hizmet getir (bulunamazsa 404 fırlatır)
    public NailService getServiceById(Long id) {
        return nailServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ID'si " + id + " olan hizmet bulunamadı!"));
    }

    // Yeni hizmet oluştur
    public NailService createService(NailService newService) {
        newService.setId(null); // Gelen ID'yi sıfırla, veritabanı kendisi atsın
        return nailServiceRepository.save(newService);
    }

    // Mevcut hizmeti güncelle
    public NailService updateService(Long id, NailService updatedService) {
        NailService existing = getServiceById(id); // 404 kontrolü dahil
        existing.setName(updatedService.getName());
        existing.setPrice(updatedService.getPrice());
        existing.setDurationInMinutes(updatedService.getDurationInMinutes());
        return nailServiceRepository.save(existing);
    }

    // Hizmet sil
    public void deleteService(Long id) {
        if (!nailServiceRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Silinecek ID'si " + id + " olan hizmet bulunamadı!");
        }
        nailServiceRepository.deleteById(id);
    }
}
