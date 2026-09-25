package com.alper.nailbar.dto;

import com.alper.nailbar.model.enums.AppointmentStatus;

import java.time.LocalDateTime;

// Frontend'e randevu bilgisini dönerken kullanılan temiz veri şablonu
public class AppointmentResponseDto {

    private Long id;
    private String customerName;
    private String customerPhone;
    private String serviceName;       // Entity yerine sadece servis adı
    private Double servicePrice;      // Fiyat bilgisini de ekleyelim
    private LocalDateTime appointmentTime;
    private LocalDateTime appointmentEndTime; // Bitiş saati artık hesaplanmış olarak dönüyor!
    private AppointmentStatus status;         // Randevu durumu (PENDING, APPROVED, vb.)
    private String staffName;                 // Randevuyu yapacak personelin adı (yoksa null)

    // Boş Constructor
    public AppointmentResponseDto() {
    }

    // Geriye dönük uyumlu 7-parametre constructor (mevcut çağrılar için)
    public AppointmentResponseDto(Long id, String customerName, String customerPhone,
                                  String serviceName, Double servicePrice,
                                  LocalDateTime appointmentTime, LocalDateTime appointmentEndTime) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.appointmentTime = appointmentTime;
        this.appointmentEndTime = appointmentEndTime;
        this.status = null;
        this.staffName = null;
    }

    // Tam Dolu Constructor (status ve staffName dahil)
    public AppointmentResponseDto(Long id, String customerName, String customerPhone,
                                  String serviceName, Double servicePrice,
                                  LocalDateTime appointmentTime, LocalDateTime appointmentEndTime,
                                  AppointmentStatus status, String staffName) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.appointmentTime = appointmentTime;
        this.appointmentEndTime = appointmentEndTime;
        this.status = status;
        this.staffName = staffName;
    }

    // Getter ve Setter Metotları
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Double getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(Double servicePrice) {
        this.servicePrice = servicePrice;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public LocalDateTime getAppointmentEndTime() {
        return appointmentEndTime;
    }

    public void setAppointmentEndTime(LocalDateTime appointmentEndTime) {
        this.appointmentEndTime = appointmentEndTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
}
