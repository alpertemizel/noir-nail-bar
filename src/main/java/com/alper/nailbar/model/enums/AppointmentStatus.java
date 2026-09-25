package com.alper.nailbar.model.enums;

/**
 * Randevu durumu.
 * PENDING:   Müşteri aldı, henüz onaylanmadı.
 * APPROVED:  Uzman / admin onayladı.
 * CANCELLED: İptal edildi.
 * COMPLETED: Hizmet tamamlandı.
 */
public enum AppointmentStatus {
    PENDING,
    APPROVED,
    CANCELLED,
    COMPLETED
}
