package com.alper.nailbar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "nail_services") // PostgreSQL'de oluşacak tablonun adı
public class NailService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Otomatik artan benzersiz ID (Serial)
    private Long id;

    @NotBlank(message = "Hizmet adı boş olamaz.")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Fiyat boş olamaz.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat sıfırdan büyük olmalıdır.")
    @Column(nullable = false)
    private Double price; // double yerine Double yaptık (Null güvenliği için)

    @NotNull(message = "Süre boş olamaz.")
    @Min(value = 1, message = "Süre en az 1 dakika olmalıdır.")
    @Column(name = "duration_in_minutes", nullable = false)
    private Integer durationInMinutes; // int yerine Integer yaptık

    // Boş Constructor (Hibernate verileri okurken bu kapıyı mutlaka arar)
    public NailService() {
    }

    // Dolu Constructor
    public NailService(Long id, String name, Double price, Integer durationInMinutes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationInMinutes = durationInMinutes;
    }

    // GETTER VE SETTER METOTLARI
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(Integer durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }
}