package com.alper.nailbar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomerNoteRequestDto {

    @NotBlank(message = "Müşteri adı boş olamaz.")
    private String customerName;

    @NotBlank(message = "Telefon numarası boş olamaz.")
    @Pattern(regexp = "^05\\d{9}$", message = "Telefon '05XXXXXXXXX' formatında olmalıdır.")
    private String customerPhone;

    @NotBlank(message = "Not metni boş olamaz.")
    private String noteText;

    public CustomerNoteRequestDto() {
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }
}
