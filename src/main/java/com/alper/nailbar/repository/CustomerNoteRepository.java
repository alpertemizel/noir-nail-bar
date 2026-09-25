package com.alper.nailbar.repository;

import com.alper.nailbar.model.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    // Telefon numarasına göre müşterinin tüm notlarını getirir
    List<CustomerNote> findByCustomerPhone(String customerPhone);
}
