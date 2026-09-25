package com.alper.nailbar.repository;

import com.alper.nailbar.model.NailService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NailServiceRepository extends JpaRepository<NailService, Long> {
}