package com.alper.nailbar.exception;

// İş kuralları ihlali için özel hata sınıfı (400 Bad Request)
// Örn: Randevu saati çakışması, mesai dışı saat
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
