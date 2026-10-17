package com.pawsclaws.petshop.config;

import com.pawsclaws.petshop.domain.*;
import com.pawsclaws.petshop.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
//@Profile("dev")
public class TestDataSeeder {

    private static final String PETSHOP_SLUG = "petly-demo";
    private static final Logger log = LoggerFactory.getLogger(PlatformAdminSeeder.class);

    private final PetshopRepository petshopRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final ComboRepository comboRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final String emailStaff;
    private final String emailAdmin;
    private final String stringPassword;

    public TestDataSeeder(
            PetshopRepository petshopRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            PetRepository petRepository,
            ServiceItemRepository serviceItemRepository,
            ComboRepository comboRepository,
            TeamMemberRepository teamMemberRepository,
            AppointmentRepository appointmentRepository,
            PasswordEncoder passwordEncoder,
            @Value("${DEMO_EMAIL_STAFF}") String emailStaff,
            @Value("${DEMO_EMAIL_ADMIN}") String emailAdmin,
            @Value("${PLATFORM_ADMIN_PASSWORD}") String stringPassword
    ) {
        this.petshopRepository = petshopRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.comboRepository = comboRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailStaff = emailStaff;
        this.emailAdmin = emailAdmin;
        this.stringPassword = stringPassword;
    }

    @PostConstruct
    public void seed() {
        Petshop petshop = petshopRepository
                .findBySlug(PETSHOP_SLUG)
                .orElseGet(this::createPetshop);

        seedUsers(petshop);
        seedClients(petshop);
        seedPets(petshop);
        seedServices(petshop);
        seedCombos(petshop);
        seedTeam(petshop);
        seedAppointments(petshop);
        log.info("Created tests users");
    }

    private Petshop createPetshop() {
        Petshop petshop = new Petshop();
        petshop.setName("Petly Pet Care");
        petshop.setSlug(PETSHOP_SLUG);
        petshop.setEmail("contato@petlydemo.com");
        petshop.setPhone("(11) 99999-1234");
        petshop.setAddress(
                "Rua das Patinhas, 123 - Centro, São Paulo - SP"
        );
        petshop.setCnpj("12345678000199");
        petshop.setLogoUrl(null);
        petshop.setPlatform(false);
        petshop.setActive(true);

        return petshopRepository.save(petshop);
    }

    private void seedUsers(Petshop petshop) {
        if (userRepository.countByPetshopId(petshop.getId()) > 0) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setPetshop(petshop);
        admin.setName("Mariana Oliveira");
        admin.setEmail(emailAdmin);
        admin.setPasswordHash(
                passwordEncoder.encode(stringPassword)
        );
        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        AppUser staff = new AppUser();
        staff.setPetshop(petshop);
        staff.setName("Rafael Santos");
        staff.setEmail(emailStaff);
        staff.setPasswordHash(
                passwordEncoder.encode(stringPassword)
        );
        staff.setRole(Role.STAFF);
        staff.setActive(true);

        userRepository.saveAll(List.of(admin, staff));
    }

    private void seedClients(Petshop petshop) {
        if (clientRepository.countByPetshopId(petshop.getId()) > 0) {
            return;
        }

        Client c1 = new Client();
        c1.setPetshop(petshop);
        c1.setName("Ana Costa");
        c1.setEmail("ana@example.com");
        c1.setPhone("(11) 98888-1001");
        c1.setNotes("Prefere agendamentos no período da manhã.");

        Client c2 = new Client();
        c2.setPetshop(petshop);
        c2.setName("Bruno Almeida");
        c2.setEmail("bruno@example.com");
        c2.setPhone("(11) 98888-1002");
        c2.setNotes("Cachorro fica ansioso com secador.");

        Client c3 = new Client();
        c3.setPetshop(petshop);
        c3.setName("Carolina Mendes");
        c3.setEmail("carolina@example.com");
        c3.setPhone("(11) 98888-1003");
        c3.setNotes("Cliente recorrente.");

        Client c4 = new Client();
        c4.setPetshop(petshop);
        c4.setName("Diego Ferreira");
        c4.setEmail("diego@example.com");
        c4.setPhone("(11) 98888-1004");
        c4.setNotes("Prefere receber confirmação por telefone.");

        clientRepository.saveAll(List.of(c1, c2, c3, c4));
    }

    private void seedPets(Petshop petshop) {
        if (petRepository.countByPetshopId(petshop.getId()) > 0) {
            return;
        }

        List<Client> clients =
                clientRepository.findAllByPetshopIdOrderByNameAsc(
                        petshop.getId()
                );

        if (clients.size() < 4) {
            return;
        }

        Pet p1 = new Pet();
        p1.setPetshop(petshop);
        p1.setClient(clients.get(0));
        p1.setName("Luna");
        p1.setSpecies("Cachorro");
        p1.setBreed("Golden Retriever");
        p1.setSize(SizeTier.LARGE);
        p1.setBirthDate(LocalDate.of(2021, 4, 12));
        p1.setNotes("Pelagem dourada; escovação cuidadosa.");

        Pet p2 = new Pet();
        p2.setPetshop(petshop);
        p2.setClient(clients.get(0));
        p2.setName("Mingau");
        p2.setSpecies("Gato");
        p2.setBreed("SRD");
        p2.setSize(SizeTier.SMALL);
        p2.setBirthDate(LocalDate.of(2023, 8, 5));
        p2.setNotes("Gato doméstico de pelo curto.");

        Pet p3 = new Pet();
        p3.setPetshop(petshop);
        p3.setClient(clients.get(1));
        p3.setName("Thor");
        p3.setSpecies("Cachorro");
        p3.setBreed("Shih Tzu");
        p3.setSize(SizeTier.SMALL);
        p3.setBirthDate(LocalDate.of(2022, 2, 18));
        p3.setNotes("Sensível ao barulho do secador.");

        Pet p4 = new Pet();
        p4.setPetshop(petshop);
        p4.setClient(clients.get(2));
        p4.setName("Mel");
        p4.setSpecies("Cachorro");
        p4.setBreed("Poodle");
        p4.setSize(SizeTier.MEDIUM);
        p4.setBirthDate(LocalDate.of(2020, 11, 9));
        p4.setNotes("Precisa de tosa higiênica regular.");

        Pet p5 = new Pet();
        p5.setPetshop(petshop);
        p5.setClient(clients.get(3));
        p5.setName("Bob");
        p5.setSpecies("Cachorro");
        p5.setBreed("Labrador");
        p5.setSize(SizeTier.LARGE);
        p5.setBirthDate(LocalDate.of(2019, 6, 20));
        p5.setNotes("Muito sociável.");

        petRepository.saveAll(List.of(p1, p2, p3, p4, p5));
    }

    private void seedServices(Petshop petshop) {
        if (serviceItemRepository.countByPetshopId(petshop.getId()) > 0) {
            return;
        }

        ServiceItem banho = new ServiceItem();
        banho.setPetshop(petshop);
        banho.setName("Banho");
        banho.setDescription("Banho com shampoo adequado à pelagem.");
        banho.setDurationMinutes(60);
        banho.setPriceSmall(new BigDecimal("45.00"));
        banho.setPriceMedium(new BigDecimal("65.00"));
        banho.setPriceLarge(new BigDecimal("85.00"));

        ServiceItem tosa = new ServiceItem();
        tosa.setPetshop(petshop);
        tosa.setName("Tosa completa");
        tosa.setDescription("Tosa de pelagem e acabamento.");
        tosa.setDurationMinutes(90);
        tosa.setPriceSmall(new BigDecimal("70.00"));
        tosa.setPriceMedium(new BigDecimal("95.00"));
        tosa.setPriceLarge(new BigDecimal("130.00"));

        ServiceItem higienica = new ServiceItem();
        higienica.setPetshop(petshop);
        higienica.setName("Tosa higiênica");
        higienica.setDescription("Higienização das regiões necessárias.");
        higienica.setDurationMinutes(30);
        higienica.setPriceSmall(new BigDecimal("30.00"));
        higienica.setPriceMedium(new BigDecimal("40.00"));
        higienica.setPriceLarge(new BigDecimal("50.00"));

        ServiceItem unhas = new ServiceItem();
        unhas.setPetshop(petshop);
        unhas.setName("Corte de unhas");
        unhas.setDescription("Corte e acabamento das unhas.");
        unhas.setDurationMinutes(15);
        unhas.setPriceSmall(new BigDecimal("15.00"));
        unhas.setPriceMedium(new BigDecimal("20.00"));
        unhas.setPriceLarge(new BigDecimal("25.00"));

        serviceItemRepository.saveAll(
                List.of(banho, tosa, higienica, unhas)
        );
    }

    private void seedCombos(Petshop petshop) {
        if (comboRepository.countByPetshopId(petshop.getId()) > 0) {
            return;
        }

        Combo combo1 = new Combo();
        combo1.setPetshop(petshop);
        combo1.setName("Banho + Tosa Higiênica");
        combo1.setDescription(
                "Banho completo e tosa higiênica no mesmo atendimento."
        );
        combo1.setPriceFrom(new BigDecimal("65.00"));
        combo1.setPopularity(95);

        Combo combo2 = new Combo();
        combo2.setPetshop(petshop);
        combo2.setName("Dia de Beleza");
        combo2.setDescription(
                "Banho, tosa completa e corte de unhas."
        );
        combo2.setPriceFrom(new BigDecimal("110.00"));
        combo2.setPopularity(80);

        Combo combo3 = new Combo();
        combo3.setPetshop(petshop);
        combo3.setName("Banho Recorrente");
        combo3.setDescription(
                "Pacote promocional para banhos frequentes."
        );
        combo3.setPriceFrom(new BigDecimal("40.00"));
        combo3.setPopularity(70);

        comboRepository.saveAll(List.of(combo1, combo2, combo3));
    }

    private void seedTeam(Petshop petshop) {
        if (teamMemberRepository.countByPetshopId(petshop.getId()) > 0) {
            return;
        }

        TeamMember t1 = new TeamMember();
        t1.setPetshop(petshop);
        t1.setName("Rafael Santos");
        t1.setRole("Tosador");
        t1.setSpecialty("Tosa na tesoura");
        t1.setRating("4.9");
        t1.setDailyCapacity(8);
        t1.setActive(true);

        TeamMember t2 = new TeamMember();
        t2.setPetshop(petshop);
        t2.setName("Juliana Ribeiro");
        t2.setRole("Banhista");
        t2.setSpecialty("Banho e cuidados com pelagem");
        t2.setRating("4.8");
        t2.setDailyCapacity(10);
        t2.setActive(true);

        TeamMember t3 = new TeamMember();
        t3.setPetshop(petshop);
        t3.setName("Pedro Martins");
        t3.setRole("Tosador");
        t3.setSpecialty("Tosa higiênica e acabamento");
        t3.setRating("4.7");
        t3.setDailyCapacity(7);
        t3.setActive(true);

        teamMemberRepository.saveAll(List.of(t1, t2, t3));
    }

    private void seedAppointments(Petshop petshop) {
        if (appointmentRepository.countByPetshopId(petshop.getId()) > 0) {
            return;
        }

        List<Client> clients =
                clientRepository.findAllByPetshopIdOrderByNameAsc(
                        petshop.getId()
                );

        List<Pet> pets =
                petRepository.findAllByPetshopIdOrderByNameAsc(
                        petshop.getId()
                );

        List<ServiceItem> services =
                serviceItemRepository.findAllByPetshopIdOrderByNameAsc(
                        petshop.getId()
                );

        List<TeamMember> team =
                teamMemberRepository.findAllByPetshopIdOrderByNameAsc(
                        petshop.getId()
                );

        if (clients.size() < 4 || pets.size() < 5
                || services.size() < 4 || team.size() < 3) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Appointment a1 = new Appointment();
        a1.setPetshop(petshop);
        a1.setClient(clients.get(0));
        a1.setPet(pets.get(0));
        a1.setService("Banho");
        a1.setScheduledAt(now.minusDays(2));
        a1.setDurationMinutes(60);
        a1.setPrice(new BigDecimal("85.00"));
        a1.setStatus(AppointmentStatus.COMPLETED);
        a1.setStaffName(team.get(0).getName());
        a1.setNotes("Atendimento concluído.");

        Appointment a2 = new Appointment();
        a2.setPetshop(petshop);
        a2.setClient(clients.get(1));
        a2.setPet(pets.get(2));
        a2.setService("Tosa completa");
        a2.setScheduledAt(now.minusDays(1));
        a2.setDurationMinutes(90);
        a2.setPrice(new BigDecimal("70.00"));
        a2.setStatus(AppointmentStatus.COMPLETED);
        a2.setStaffName(team.get(1).getName());
        a2.setNotes("Cliente satisfeito com o resultado.");

        Appointment a3 = new Appointment();
        a3.setPetshop(petshop);
        a3.setClient(clients.get(2));
        a3.setPet(pets.get(3));
        a3.setService("Tosa higiênica");
        a3.setScheduledAt(now.minusHours(1));
        a3.setDurationMinutes(30);
        a3.setPrice(new BigDecimal("40.00"));
        a3.setStatus(AppointmentStatus.IN_PROGRESS);
        a3.setStaffName(team.get(2).getName());
        a3.setNotes("Atendimento em andamento.");

        Appointment a4 = new Appointment();
        a4.setPetshop(petshop);
        a4.setClient(clients.get(3));
        a4.setPet(pets.get(4));
        a4.setService("Banho");
        a4.setScheduledAt(now.plusHours(2));
        a4.setDurationMinutes(60);
        a4.setPrice(new BigDecimal("85.00"));
        a4.setStatus(AppointmentStatus.SCHEDULED);
        a4.setStaffName(team.get(1).getName());
        a4.setNotes("Agendamento de demonstração.");

        Appointment a5 = new Appointment();
        a5.setPetshop(petshop);
        a5.setClient(clients.get(0));
        a5.setPet(pets.get(1));
        a5.setService("Corte de unhas");
        a5.setScheduledAt(now.plusDays(1));
        a5.setDurationMinutes(15);
        a5.setPrice(new BigDecimal("15.00"));
        a5.setStatus(AppointmentStatus.CANCELLED);
        a5.setStaffName(team.get(0).getName());
        a5.setNotes("Cancelamento de demonstração.");

        appointmentRepository.saveAll(
                List.of(a1, a2, a3, a4, a5)
        );
    }
}
