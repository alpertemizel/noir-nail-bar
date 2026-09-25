# Noir Nail Bar

Gerçek bir tırnak bakım stüdyosu için geliştirilen full-stack salon yönetim sistemi. Müşteriler web arayüzü veya yapay zeka destekli chatbot üzerinden randevu alabilir; salon yöneticisi admin panelinden personel, vardiya ve gelir analitiğini yönetebilir.

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20AI-2.0.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Google%20Gemini-AI-4285F4?style=for-the-badge&logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</p>

---

## İçindekiler

- [Proje Hakkında](#proje-hakkında)
- [Temel Özellikler](#temel-özellikler)
- [Teknoloji Yığını](#teknoloji-yığını)
- [Mimari Yapı](#mimari-yapı)
- [Veritabanı Şeması](#veritabanı-şeması)
- [API Endpoint'leri](#api-endpointleri)
- [Kurulum](#kurulum)
- [Proje İstatistikleri](#proje-istatistikleri)

---

## Proje Hakkında

**Noir Nail Bar**, müşteri randevu sürecini uçtan uca dijitalleştiren bir salon yönetim platformudur. Projenin ayırt edici özelliği, **Spring AI Function Calling** altyapısıyla geliştirilmiş AI chatbot'udur. Chatbot sıradan bir sohbet aracı değil — veritabanıyla doğrudan etkileşime giren otonom bir AI Agent olarak çalışır:

- Hizmet kataloğunu ve güncel fiyatları veritabanından çeker
- Müsait randevu saatlerini sorgulayıp müşteriye sunar
- Müşteriden gerekli bilgileri alarak doğrudan veritabanına randevu kaydı oluşturur
- Konuşma geçmişini oturum bazlı bellek ile korur (son 20 mesaj penceresi)

---

## Temel Özellikler

### Müşteri Tarafı
- 4 adımlı randevu sihirbazı — Hizmet Seç → Tarih Seç → Saat Seç → Bilgi Gir
- AI Chatbot ile doğal dilde randevu alma
- Personel seçimi (opsiyonel)
- Gerçek zamanlı müsaitlik kontrolü

### Personel (STAFF) Paneli
- Günlük / filtrelenebilir randevu takvimi
- Randevu durumu güncelleme (Onay, İptal, Tamamla)
- Personel atama ve randevu erteleme
- Müşteri notları yönetimi (alerji, tercih, hassasiyet)
- Kendi vardiya programını görüntüleme

### Admin (SUPER_ADMIN) Paneli
- **Dashboard** — Günlük istatistikler (randevu, bekleyen, aktif personel, tamamlanan)
- **Aylık analitik** — Toplam/ortalama ciro, personel & hizmet bazlı gelir dağılımı
- Personel yönetimi (kayıt, aktif/pasif, silme)
- Vardiya yönetimi (haftalık çalışma saatleri, izin günleri)
- Hizmet kataloğu yönetimi (fiyat, süre, ekleme/silme)

### Güvenlik
- JWT Authentication — Stateless, Bearer token tabanlı kimlik doğrulama
- BCrypt ile şifre hashleme
- Role-Based Access Control — Endpoint seviyesinde SUPER_ADMIN / STAFF yetki ayrımı
- CSRF devre dışı (stateless API), CORS yapılandırması

---

## Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| **Backend Framework** | Spring Boot 4.1.0, Spring MVC |
| **Yapay Zeka** | Spring AI 2.0.0, Google Gemini (gemini-3.6-flash) |
| **AI Araçları** | Spring AI Function Calling (`@Tool`) |
| **Güvenlik** | Spring Security 7, JWT (JJWT 0.12.6) |
| **Veritabanı** | PostgreSQL 15, Spring Data JPA / Hibernate |
| **Doğrulama** | Jakarta Bean Validation (`@NotBlank`, `@Future`, `@Pattern`) |
| **Konteyner** | Docker Compose |
| **Frontend** | Vanilla HTML5 / CSS3 / JavaScript (SPA benzeri) |
| **Dil** | Java 21 |

---

## Mimari Yapı

Proje katmanlı mimari (Layered Architecture) prensibine uygun geliştirilmiştir:

```
src/main/java/com/alper/nailbar/
├── config/                  # Uygulama yapılandırmaları
│   ├── ChatClientConfig     # Spring AI ChatClient, System Prompt, Memory
│   ├── DatabaseInitializer  # Seed data (hizmetler, admin hesabı)
│   ├── SecurityConfig       # Spring Security, JWT filtre zinciri
│   └── WebConfig            # CORS yapılandırması
│
├── controller/              # REST API katmanı (7 controller)
│   ├── AdminController      # Dashboard, analitik, personel/vardiya yönetimi
│   ├── AppointmentController# Randevu CRUD, müsait saat sorgulama
│   ├── AuthController       # Login (JWT) & Register
│   ├── ChatController       # AI Chatbot endpoint
│   ├── CustomerNoteController# Müşteri notları CRUD
│   ├── NailServiceController # Hizmet kataloğu CRUD
│   └── StaffController      # Personel paneli & public personel listesi
│
├── dto/                     # Veri transfer nesneleri (Request/Response)
│   ├── auth/                # LoginRequest/Response, RegisterRequest
│   └── chat/                # ChatRequest, ChatResponse
│
├── exception/               # Global hata yönetimi
│   ├── GlobalExceptionHandler
│   ├── AppointmentConflictException
│   └── ResourceNotFoundException
│
├── model/                   # JPA varlıkları
│   ├── Appointment          # Randevu (durum makinesi: PENDING→APPROVED→COMPLETED)
│   ├── CustomerNote         # Müşteri notları
│   ├── NailService          # Hizmet kataloğu
│   ├── Shift                # Vardiya planı
│   ├── User                 # Personel & Admin
│   └── enums/               # AppointmentStatus, Role
│
├── repository/              # Spring Data JPA repository arayüzleri
│
├── security/                # JWT altyapısı
│   ├── JwtAuthFilter        # OncePerRequestFilter — token doğrulama
│   ├── JwtUtil              # Token üretme, çözümleme, imzalama
│   └── UserDetailsServiceImpl
│
└── service/                 # İş mantığı katmanı
    ├── AppointmentService   # Randevu motoru (çakışma, müsaitlik, analitik)
    ├── ChatService          # AI iletişim ve hata yönetimi
    ├── ChatTools            # @Tool — AI Function Calling araçları
    ├── CustomerNoteService  # Not CRUD
    ├── NailServiceService   # Hizmet CRUD
    └── ShiftService         # Vardiya CRUD
```

### AI Function Calling Akışı

```
Müşteri Mesajı → ChatController → ChatService → Spring AI ChatClient
                                                        │
                                    ┌───────────────────┼───────────────────┐
                                    ▼                   ▼                   ▼
                           getServicesAndPrices()  getAvailableSlots()  createAppointment()
                              (ChatTools)            (ChatTools)          (ChatTools)
                                    │                   │                   │
                                    ▼                   ▼                   ▼
                          NailServiceRepository  AppointmentService  AppointmentService
                                                                          │
                                                                          ▼
                                                                    PostgreSQL DB
```

---

## Veritabanı Şeması

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│      users       │     │   nail_services  │     │  customer_notes  │
├──────────────────┤     ├──────────────────┤     ├──────────────────┤
│ id          PK   │     │ id          PK   │     │ id          PK   │
│ name             │     │ name             │     │ customer_name    │
│ email    UNIQUE  │     │ price            │     │ customer_phone   │
│ password (BCrypt)│     │ duration_in_min  │     │ note_text   TEXT │
│ phone            │     └──────┬───────────┘     │ created_at       │
│ role    ENUM     │            │                 └──────────────────┘
│ active  BOOLEAN  │            │
└──────┬───────────┘            │
       │                        │
       │ staff_id (nullable)    │ nail_service_id
       │                        │
┌──────▼────────────────────────▼──┐
│           appointments           │
├──────────────────────────────────┤
│ id                          PK   │
│ customer_name                    │
│ customer_phone                   │
│ nail_service_id             FK   │
│ staff_id            FK NULLABLE  │
│ appointment_time   DATETIME      │
│ status  CHECK(PENDING|APPROVED|  │
│         CANCELLED|COMPLETED)     │
│ reminder_sent_at   DATETIME      │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│             shifts               │
├──────────────────────────────────┤
│ id                          PK   │
│ staff_id                    FK   │
│ day_of_week          ENUM(M-Su)  │
│ start_time               TIME    │
│ end_time                 TIME    │
│ is_off_day            BOOLEAN    │
└──────────────────────────────────┘
```

---

## API Endpoint'leri

### Herkese Açık

| Metot | Endpoint | Açıklama |
|---|---|---|
| `POST` | `/api/auth/login` | JWT token ile giriş |
| `POST` | `/api/appointments` | Yeni randevu oluştur |
| `GET` | `/api/appointments/available-slots` | Müsait saatleri sorgula |
| `GET` | `/api/services` | Hizmet kataloğunu listele |
| `GET` | `/api/staff/public` | Aktif personel listesi |
| `POST` | `/api/chat` | AI Chatbot ile konuş |

### STAFF & SUPER_ADMIN

| Metot | Endpoint | Açıklama |
|---|---|---|
| `GET` | `/api/appointments` | Randevuları listele (tarih filtreli) |
| `PUT` | `/api/appointments/{id}/status` | Durum güncelle |
| `PUT` | `/api/appointments/{id}/staff` | Personel ata |
| `PUT` | `/api/appointments/{id}/reschedule` | Randevu ertele |
| `DELETE` | `/api/appointments/{id}` | Randevu sil |
| `CRUD` | `/api/customer-notes/**` | Müşteri notları yönetimi |

### Sadece SUPER_ADMIN

| Metot | Endpoint | Açıklama |
|---|---|---|
| `POST` | `/api/auth/register` | Yeni personel kaydı |
| `GET` | `/api/admin/dashboard` | Günlük istatistikler |
| `GET` | `/api/admin/analytics` | Aylık gelir & performans raporu |
| `GET/PUT/DELETE` | `/api/admin/users/**` | Personel yönetimi |
| `CRUD` | `/api/admin/shifts/**` | Vardiya yönetimi |
| `POST/PUT/DELETE` | `/api/services/**` | Hizmet yönetimi |

---

## Kurulum

### Gereksinimler

- Java 21+
- Docker & Docker Compose
- Google Gemini API Key ([Google AI Studio](https://aistudio.google.com/))

### 1. Projeyi Klonlayın

```bash
git clone https://github.com/alpertemizel/noir-nail-bar.git
cd noir-nail-bar
```

### 2. PostgreSQL Veritabanını Başlatın

```bash
docker-compose up -d
```

Bu komut PostgreSQL 15 Alpine konteynerini `5432` portunda başlatır.

### 3. Ortam Değişkenlerini Ayarlayın

```bash
export GEMINI_API_KEY=<your-gemini-api-key>
# Opsiyonel: Üretim ortamı için JWT secret
export JWT_SECRET=<your-256bit-secret-key>
```

### 4. Uygulamayı Başlatın

```bash
./mvnw spring-boot:run
```

Uygulama `http://localhost:8080` adresinde ayağa kalkacaktır.

### 5. Varsayılan Hesap

| Rol | E-posta | Şifre |
|---|---|---|
| SUPER_ADMIN | `alpertemizell@gmail.com` | `admin123` |

> **Not:** Üretim ortamında varsayılan şifreyi mutlaka değiştirin.

---

## Proje İstatistikleri

| Metrik | Değer |
|---|---|
| Java Dosya Sayısı | 53 |
| Frontend Dosya Sayısı | 6 (HTML, CSS, JS) |
| Toplam Frontend Satır | ~6.700 |
| REST Endpoint Sayısı | 25+ |
| Controller Sayısı | 7 |
| Entity Sayısı | 5 |
| AI Tool Sayısı | 3 |

---

## Yol Haritası

- [ ] SMS / E-posta ile randevu hatırlatma bildirimleri
- [ ] Müşteri kayıt ve giriş sistemi
- [ ] Online ödeme entegrasyonu
- [ ] Mobil uygulama (React Native)
- [ ] Unit & Integration test kapsamının genişletilmesi

---

## Lisans

Bu proje kişisel portföy amaçlı geliştirilmiştir.

---

<p align="center">
  <b>Geliştirici:</b> Alper Temizel<br>
  <a href="mailto:alpertemizell@gmail.com">alpertemizell@gmail.com</a>
</p>
