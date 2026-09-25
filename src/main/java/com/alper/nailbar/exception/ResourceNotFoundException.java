package com.alper.nailbar.exception;

// Veritabanında bulunamayan kaynaklar için özel hata sınıfı (404 Not Found)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}


