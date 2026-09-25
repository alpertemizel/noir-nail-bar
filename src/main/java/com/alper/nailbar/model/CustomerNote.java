package com.alper.nailbar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Müşteriye ait özel notlar: Alerji, hassasiyet, ürün tercihleri vb.
 * Müşteri kimliği telefon numarasıyla eşleştirilir (sisteme kayıtlı hesap yok).
 */
@Entity
@Table(name = "customer_notes")
public class CustomerNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Müşteri adı boş olamaz.")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotBlank(message = "Telefon numarası boş olamaz.")
    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @NotBlank(message = "Not metni boş olamaz.")
    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    // Notun eklendiği tarih — otomatik set edilir, dışarıdan değiştirilemez
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Constructors ---

    public CustomerNote() {
        this.createdAt = LocalDateTime.now();
    }

    public CustomerNote(String customerName, String customerPhone, String noteText) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.noteText = noteText;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // createdAt dışarıdan değiştirilmemeli, getter yeterli
}
