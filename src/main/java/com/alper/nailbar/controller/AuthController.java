package com.alper.nailbar.controller;

import com.alper.nailbar.dto.auth.LoginRequestDto;
import com.alper.nailbar.dto.auth.LoginResponseDto;
import com.alper.nailbar.dto.auth.RegisterRequestDto;
import com.alper.nailbar.model.User;
import com.alper.nailbar.repository.UserRepository;
import com.alper.nailbar.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/auth/login
     * Herkese açık — e-posta + şifre ile giriş yap, JWT döner.
     *
     * Örnek istek:
     * {
     *   "email": "admin@noirnailbar.com",
     *   "password": "admin123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        // Spring Security kimlik doğrulamasını tetikle (yanlış şifre → AuthenticationException → 401)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // DB'den tam User objesini çek (rol ve isim bilgisi için)
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDto(token, user.getName(), user.getRole()));
    }

    /**
     * POST /api/auth/register
     * Sadece SUPER_ADMIN çağırabilir — yeni personel (STAFF) veya admin kaydeder.
     *
     * Örnek istek:
     * {
     *   "name": "Ayşe Uzman",
     *   "email": "ayse@noirnailbar.com",
     *   "password": "sifre1234",
     *   "phone": "05551234567",
     *   "role": "STAFF"
     * }
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDto request) {
        // E-posta benzersizlik kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "'" + request.getEmail() + "' e-postası zaten kullanımda.");
        }

        User newUser = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()), // Şifreyi BCrypt ile hashle!
                request.getPhone(),
                request.getRole()
        );

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Kullanıcı başarıyla oluşturuldu.", "email", request.getEmail()));
    }
}
