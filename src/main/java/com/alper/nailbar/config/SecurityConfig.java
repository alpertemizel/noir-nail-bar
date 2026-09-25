package com.alper.nailbar.config;

import com.alper.nailbar.security.JwtAuthFilter;
import com.alper.nailbar.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security yapılandırması.
 * Stateless JWT mimarisi — session/cookie kullanılmaz.
 * @EnableMethodSecurity ile controller'larda @PreAuthorize kullanılabilir.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF — stateless JWT mimarisinde gerekmez
            .csrf(AbstractHttpConfigurer::disable)

            // CORS — WebConfig.java'daki MvcCorsConfigurer kullanılır; Security buna uyar
            .cors(cors -> {}) // MvcCorsConfigurer'ı devral

            // Session — stateless (her istek kendi token'ını taşır)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Endpoint Yetkilendirme Kuralları
            .authorizeHttpRequests(auth -> auth

                // Statik dosyalar — herkese açık (HTML, CSS, JS, resimler)
                .requestMatchers("/", "/index.html", "/admin.html", "/staff.html", "/login.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // Auth endpoint'leri — herkese açık
                .requestMatchers("/api/auth/**").permitAll()

                // Personel listesi — herkese açık (müşteri randevu seçimi için)
                .requestMatchers(HttpMethod.GET, "/api/staff/public").permitAll()

                // Hizmet listesi — herkese açık (müşteriler randevu alırken görür)
                .requestMatchers(HttpMethod.GET, "/api/services", "/api/services/**").permitAll()

                // Randevu alma (POST) ve boş saat sorgulama — herkese açık
                .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/appointments/available-slots").permitAll()

                // AI Chatbot — herkese açık (müşteriler giriş yapmadan kullanabilir)
                .requestMatchers("/api/chat/**").permitAll()

                // Tüm randevuları listeleme — sadece giriş yapan kullanıcılar
                .requestMatchers(HttpMethod.GET, "/api/appointments").hasAnyRole("SUPER_ADMIN", "STAFF")

                // Randevu durumu güncelleme — sadece giriş yapan kullanıcılar
                .requestMatchers(HttpMethod.PUT, "/api/appointments/**").hasAnyRole("SUPER_ADMIN", "STAFF")

                // Randevu silme / iptal — SUPER_ADMIN veya STAFF
                .requestMatchers(HttpMethod.DELETE, "/api/appointments/**").hasAnyRole("SUPER_ADMIN", "STAFF")

                // Hizmet yönetimi (oluştur/güncelle/sil) — sadece SUPER_ADMIN
                .requestMatchers(HttpMethod.POST, "/api/services/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/services/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasRole("SUPER_ADMIN")

                // Admin paneli — sadece SUPER_ADMIN
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                // Staff paneli — SUPER_ADMIN veya STAFF
                .requestMatchers("/api/staff/**").hasAnyRole("SUPER_ADMIN", "STAFF")

                // Müşteri notları — sadece giriş yapan kullanıcılar
                .requestMatchers("/api/customer-notes/**").hasAnyRole("SUPER_ADMIN", "STAFF")

                // Geri kalan her şey — kimlik doğrulama gerekli
                .anyRequest().authenticated()
            )

            // JWT Filter'ı UsernamePasswordAuthenticationFilter'dan önce ekle
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
