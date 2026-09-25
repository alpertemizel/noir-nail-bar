package com.alper.nailbar.config;

import com.alper.nailbar.model.NailService;
import com.alper.nailbar.model.User;
import com.alper.nailbar.model.enums.Role;
import com.alper.nailbar.repository.NailServiceRepository;
import com.alper.nailbar.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final NailServiceRepository nailServiceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(NailServiceRepository nailServiceRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.nailServiceRepository = nailServiceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        removeOldNoirAdmin();
        seedNailServices();
        seedSuperAdmin();
    }

    // --- Eski "Noir Admin" hesabını kaldır ---
    private void removeOldNoirAdmin() {
        String oldAdminEmail = "admin@noirnailbar.com";
        userRepository.findByEmail(oldAdminEmail).ifPresent(user -> {
            userRepository.delete(user);
            System.out.println(">> Eski 'Noir Admin' hesabı (" + oldAdminEmail + ") veritabanından silindi.");
        });
    }

    // --- Nail Service Başlangıç Verileri ---
    private void seedNailServices() {
        // Eğer veritabanında henüz hiç hizmet tanımlı değilse, başlangıç verilerini yükle
        if (nailServiceRepository.count() == 0) {
            List<NailService> defaultServices = Arrays.asList(
                    new NailService(null, "Protez Tırnak", 1900.0, 120),
                    new NailService(null, "Protez Bakım", 1700.0, 90),
                    new NailService(null, "Şablon Protez", 2200.0, 120),
                    new NailService(null, "Jel Güçlendirme", 1600.0, 60),
                    new NailService(null, "Kalıcı Oje", 1400.0, 45),
                    new NailService(null, "Manikür", 1000.0, 45),
                    new NailService(null, "Protez Çıkartma", 500.0, 30),
                    new NailService(null, "Pedikür Kalıcı Oje", 1500.0, 60),
                    new NailService(null, "Pedikür Jel", 1700.0, 60),
                    new NailService(null, "Pedikür", 1200.0, 45),
                    new NailService(null, "Pedikür Çıkartma", 500.0, 30)
            );

            nailServiceRepository.saveAll(defaultServices);
            System.out.println(">> Başlangıç Nail Service verileri PostgreSQL veritabanına başarıyla yüklendi.");
        }
    }

    // --- SUPER_ADMIN Başlangıç Hesabı ---
    private void seedSuperAdmin() {
        String adminEmail = "alpertemizell@gmail.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User superAdmin = new User(
                    "Alper Temizel",
                    adminEmail,
                    passwordEncoder.encode("admin123"), // Şifre BCrypt ile hash'leniyor!
                    null,
                    Role.SUPER_ADMIN
            );
            userRepository.save(superAdmin);
            System.out.println(">> SUPER_ADMIN hesabı oluşturuldu.");
            System.out.println("   E-posta : " + adminEmail);
            System.out.println("   Şifre   : admin123  (İlk girişten sonra mutlaka değiştirin!)");
        }
    }
}
