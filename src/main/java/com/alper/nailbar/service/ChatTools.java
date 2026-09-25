package com.alper.nailbar.service;

import com.alper.nailbar.dto.AppointmentRequestDto;
import com.alper.nailbar.dto.AppointmentResponseDto;
import com.alper.nailbar.dto.AvailableSlotDto;
import com.alper.nailbar.model.NailService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Chatbot'un veritabanı ve iş katmanıyla etkileşim kurmasını sağlayan tool metotları.
 * <p>
 * Spring AI {@code @Tool} anotasyonu ile işaretlenen metotlar, LLM tarafından
 * gerekli görüldüğünde otomatik olarak çağrılır (Function Calling).
 * AI, müşterinin niyetini anlayarak doğru fonksiyonu doğru parametrelerle tetikler.
 */
@Component
public class ChatTools {

    private final NailServiceService nailServiceService;
    private final AppointmentService appointmentService;

    public ChatTools(NailServiceService nailServiceService, AppointmentService appointmentService) {
        this.nailServiceService = nailServiceService;
        this.appointmentService = appointmentService;
    }

    /**
     * Veritabanındaki tüm hizmetleri (ad, fiyat, süre) döndürür.
     * Müşteri "Hangi hizmetleriniz var?", "Fiyatlar ne kadar?", "Ne kadar sürüyor?" gibi
     * sorular sorduğunda AI bu fonksiyonu tetikler.
     */
    @Tool(description = """
            Noir Nail Bar'ın güncel hizmet listesini veritabanından getirir.
            Her hizmet için ad, fiyat (TL) ve süre (dakika) bilgisi döner.
            Müşteri hizmetler, fiyatlar veya süreler hakkında soru sorduğunda bu aracı kullan.
            """)
    public String getServicesAndPrices() {
        List<NailService> services = nailServiceService.getAllServices();

        if (services.isEmpty()) {
            return "Şu anda sistemde kayıtlı hizmet bulunmamaktadır.";
        }

        StringBuilder sb = new StringBuilder("Güncel Hizmet Listesi:\n");
        for (NailService s : services) {
            sb.append(String.format("- %s: %.0f TL, yaklaşık %d dakika%n",
                    s.getName(), s.getPrice(), s.getDurationInMinutes()));
        }
        return sb.toString();
    }

    /**
     * Belirtilen tarih ve hizmet için müsait randevu saatlerini döndürür.
     * Müşteri "Yarın müsait saat var mı?", "Manikür için ne zaman gelebilirim?" gibi
     * sorular sorduğunda AI bu fonksiyonu tetikler.
     */
    @Tool(description = """
            Belirtilen tarih ve hizmet ID'si için müsait randevu saatlerini veritabanından getirir.
            Müşteri belirli bir gün için boş saat sorduğunda bu aracı kullan.
            Önce getServicesAndPrices ile hizmet listesini çek ve doğru hizmet ID'sini belirle.
            Tarih formatı: YYYY-MM-DD (örn: 2026-08-20).
            """)
    public String getAvailableSlots(
            @ToolParam(description = "Randevu tarihi, YYYY-MM-DD formatında (örn: 2026-08-20)") String date,
            @ToolParam(description = "Hizmetin veritabanı ID'si (getServicesAndPrices'tan alınır)") Long serviceId) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);

            // Geçmiş tarih kontrolü
            if (parsedDate.isBefore(LocalDate.now())) {
                return "Geçmiş bir tarih için randevu sorgulanamaz. Lütfen bugün veya sonrası bir tarih belirtin.";
            }

            List<AvailableSlotDto> slots = appointmentService.getAvailableSlots(parsedDate, serviceId, null);

            if (slots.isEmpty()) {
                return date + " tarihinde bu hizmet için müsait saat bulunmamaktadır. "
                        + "Farklı bir tarih denemek ister misiniz?";
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            String slotList = slots.stream()
                    .map(s -> s.getStartTime().format(fmt) + " - " + s.getEndTime().format(fmt))
                    .collect(Collectors.joining("\n- ", "- ", ""));

            return date + " tarihindeki müsait saatler:\n" + slotList;

        } catch (DateTimeParseException e) {
            return "Tarih formatı hatalı. Lütfen YYYY-MM-DD formatında bir tarih girin (örn: 2026-08-20).";
        } catch (Exception e) {
            return "Müsait saatler sorgulanırken bir hata oluştu: " + e.getMessage();
        }
    }

    /**
     * Müşteri adına yeni bir randevu oluşturur.
     * AI, müşteriden ad, telefon, hizmet, tarih ve saat bilgilerini topladıktan sonra
     * bu fonksiyonu çağırarak randevuyu veritabanına kaydeder.
     */
    @Tool(description = """
            Müşteri için yeni bir randevu oluşturur ve veritabanına kaydeder.
            Bu aracı kullanmadan önce mutlaka müşteriden şu bilgileri topla:
            1. Müşteri adı (ad soyad)
            2. Telefon numarası (05XXXXXXXXX formatında, 11 haneli)
            3. İstenen hizmet (getServicesAndPrices ile hizmet ID'sini belirle)
            4. Randevu tarihi ve saati (getAvailableSlots ile müsaitliği doğrula)
            Tüm bilgiler eksiksiz toplandıktan sonra randevuyu oluştur.
            Eksik bilgi varsa müşteriye sor, aracı çağırma.
            """)
    public String createAppointment(
            @ToolParam(description = "Müşterinin adı ve soyadı") String customerName,
            @ToolParam(description = "Müşterinin telefon numarası, 05XXXXXXXXX formatında") String customerPhone,
            @ToolParam(description = "Hizmetin veritabanı ID'si") Long serviceId,
            @ToolParam(description = "Randevu tarihi ve saati, YYYY-MM-DD'T'HH:mm formatında (örn: 2026-08-20T14:00)") String appointmentDateTime) {
        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(appointmentDateTime);

            // Geçmiş tarih kontrolü
            if (parsedDateTime.isBefore(LocalDateTime.now())) {
                return "Geçmiş bir tarih ve saat için randevu oluşturulamaz.";
            }

            AppointmentRequestDto requestDto = new AppointmentRequestDto();
            requestDto.setCustomerName(customerName);
            requestDto.setCustomerPhone(customerPhone);
            requestDto.setServiceId(serviceId);
            requestDto.setAppointmentTime(parsedDateTime);

            AppointmentResponseDto result = appointmentService.createAppointment(requestDto);

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

            return String.format(
                    "Randevunuz başarıyla oluşturuldu! " +
                    "Detaylar: %s, %s hizmeti, %s tarihinde saat %s. " +
                    "Randevu numaranız: %d.",
                    result.getCustomerName(),
                    result.getServiceName(),
                    result.getAppointmentTime().format(dateFmt),
                    result.getAppointmentTime().format(timeFmt),
                    result.getId()
            );

        } catch (DateTimeParseException e) {
            return "Tarih/saat formatı hatalı. Lütfen YYYY-MM-DD'T'HH:mm formatında girin (örn: 2026-08-20T14:00).";
        } catch (Exception e) {
            return "Randevu oluşturulurken bir sorun oluştu: " + e.getMessage();
        }
    }
}
