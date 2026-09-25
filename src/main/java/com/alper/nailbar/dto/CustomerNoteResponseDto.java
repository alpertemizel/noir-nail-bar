package com.alper.nailbar.dto;

import java.time.LocalDateTime;

public class CustomerNoteResponseDto {

    private Long id;
    private String customerName;
    private String customerPhone;
    private String noteText;
    private LocalDateTime createdAt;

    public CustomerNoteResponseDto() {
    }

    public CustomerNoteResponseDto(Long id, String customerName, String customerPhone,
                                   String noteText, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.noteText = noteText;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
