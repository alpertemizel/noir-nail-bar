package com.alper.nailbar.model;

import com.alper.nailbar.model.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments") // PostgreSQL'de oluşacak tablo adı
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Otomatik artan ID (Serial)
    private Long id;

    private String customerName;
    private String customerPhone;

    // Her randevunun BİR hizmeti olur, ama bir hizmet BİRDEN FAZLA randevuda bulunabilir (Many-to-One)
    @ManyToOne
    @JoinColumn(name = "nail_service_id", nullable = false) // Veri tabanındaki Foreign Key sütunu
    private NailService nailService;

    // Randevuyu yürüten personel (atanmamışsa null olabilir)
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    private LocalDateTime appointmentTime;

    // Randevu durumu — varsayılan: APPROVED (onaylı)
    // columnDefinition'daki DEFAULT ifadesi, mevcut satırlara 'APPROVED' atayarak NOT NULL kısıtının
    // geçmişte oluşturulmuş boş satırlar yüzünden başarısız olmasını önler.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'APPROVED' not null check (status in ('PENDING','APPROVED','CANCELLED','COMPLETED'))")
    private AppointmentStatus status = AppointmentStatus.APPROVED;

    // SMS/e-posta hatırlatması gönderildi mi? Gönderildiyse ne zaman?
    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    // Boş Constructor (Hibernate için mecburi)
    public Appointment() {
    }

    // Dolu Constructor
    public Appointment(Long id, String customerName, String customerPhone, NailService nailService, LocalDateTime appointmentTime) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.nailService = nailService;
        this.appointmentTime = appointmentTime;
        this.status = AppointmentStatus.APPROVED;
    }

    // GETTER VE SETTER METOTLARI
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

    public NailService getNailService() {
        return nailService;
    }

    // İleride isimlendirme standartlarından dolayı hata yaşamamak için metot adını setNailService yaptık kanka
    public void setNailService(NailService nailService) {
        this.nailService = nailService;
    }

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getReminderSentAt() {
        return reminderSentAt;
    }

    public void setReminderSentAt(LocalDateTime reminderSentAt) {
        this.reminderSentAt = reminderSentAt;
    }
}