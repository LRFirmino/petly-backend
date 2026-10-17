package com.pawsclaws.petshop.config;

import com.pawsclaws.petshop.domain.AppUser;
import com.pawsclaws.petshop.domain.Petshop;
import com.pawsclaws.petshop.domain.Role;
import com.pawsclaws.petshop.repository.PetshopRepository;
import com.pawsclaws.petshop.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Locale;

/**
 * Garante a existência da conta de <b>administrador da plataforma</b>
 * (equipe/devs do SaaS), que é quem cria petshops e contas.
 *
 * <p>As credenciais <b>não ficam no código</b>: são lidas das variáveis de
 * ambiente {@code PLATFORM_ADMIN_EMAIL} e {@code PLATFORM_ADMIN_PASSWORD}.
 * Se não estiverem configuradas, nenhuma conta é criada (e a app sobe
 * normalmente).</p>
 */
@Component
public class PlatformAdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(PlatformAdminSeeder.class);
    private static final String PLATFORM_PETSHOP_SLUG = "petly-platform";
    private static final String PLATFORM_PETSHOP_NAME = "Petly Platform";

    private final PetshopRepository petshopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final String adminEmail;
    private final String adminPassword;

    public PlatformAdminSeeder(PetshopRepository petshopRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               TransactionTemplate transactionTemplate,
                               @Value("${app.platform-admin.email:}") String adminEmail,
                               @Value("${app.platform-admin.password:}") String adminPassword) {
        this.petshopRepository = petshopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = transactionTemplate;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public void seed() {
        if (isBlank(adminEmail) || isBlank(adminPassword)) {
            log.info("Nenhum admin de plataforma configurado "
                    + "(defina PLATFORM_ADMIN_EMAIL e PLATFORM_ADMIN_PASSWORD para criá-lo).");
            return;
        }
        transactionTemplate.executeWithoutResult(status -> doSeed());
    }

    private void doSeed() {
        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Petshop platform = petshopRepository.findBySlug(PLATFORM_PETSHOP_SLUG)
                .orElseGet(this::createPlatformPetshop);

        AppUser admin = new AppUser();
        admin.setPetshop(platform);
        admin.setName("Platform Admin");
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.PLATFORM_ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        log.info("Conta de administrador da plataforma criada ({})", email);
    }

    private Petshop createPlatformPetshop() {
        Petshop petshop = new Petshop();
        petshop.setName(PLATFORM_PETSHOP_NAME);
        petshop.setSlug(PLATFORM_PETSHOP_SLUG);
        petshop.setPlatform(true);
        petshop.setActive(true);
        return petshopRepository.save(petshop);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
