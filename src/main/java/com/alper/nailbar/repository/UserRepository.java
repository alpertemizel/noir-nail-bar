package com.alper.nailbar.repository;

import com.alper.nailbar.model.User;
import com.alper.nailbar.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Security — email ile kullanıcı bulma (login için zorunlu)
    Optional<User> findByEmail(String email);

    // Kayıt validasyonu — aynı email ile ikinci kayıt engellenir
    boolean existsByEmail(String email);

    // Dashboard: Toplam aktif personel sayısı
    long countByRole(Role role);
    long countByRoleAndActiveTrue(Role role);

    // Admin: Belirli roldeki kullanıcıları listele
    List<User> findAllByRole(Role role);
    List<User> findAllByRoleAndActiveTrue(Role role);

    // Müşteri seçimi için birden fazla roldeki personelleri getir (STAFF, SUPER_ADMIN)
    List<User> findAllByRoleIn(List<Role> roles);
    List<User> findAllByRoleInAndActiveTrue(List<Role> roles);
}
