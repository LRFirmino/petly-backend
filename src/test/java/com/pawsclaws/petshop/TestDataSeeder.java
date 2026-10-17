package com.pawsclaws.petshop;

import com.pawsclaws.petshop.domain.Appointment;
import com.pawsclaws.petshop.domain.AppointmentStatus;
import com.pawsclaws.petshop.domain.AppUser;
import com.pawsclaws.petshop.domain.Client;
import com.pawsclaws.petshop.domain.Combo;
import com.pawsclaws.petshop.domain.Pet;
import com.pawsclaws.petshop.domain.Petshop;
import com.pawsclaws.petshop.domain.Role;
import com.pawsclaws.petshop.domain.ServiceItem;
import com.pawsclaws.petshop.domain.SizeTier;
import com.pawsclaws.petshop.domain.TeamMember;
import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.repository.ClientRepository;
import com.pawsclaws.petshop.repository.ComboRepository;
import com.pawsclaws.petshop.repository.PetRepository;
import com.pawsclaws.petshop.repository.PetshopRepository;
import com.pawsclaws.petshop.repository.ServiceItemRepository;
import com.pawsclaws.petshop.repository.TeamMemberRepository;
import com.pawsclaws.petshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fixtures de teste: dois petshops de exemplo com usuários, clientes, pets,
 * serviços, combos, equipe e agendamentos.
 *
 * <p>Fica em {@code src/test} de propósito — <b>não</b> é enviado na aplicação.
 * Em produção os petshops/contas são criados pelo admin da plataforma.</p>
 */
@Component
public class TestDataSeeder {

    static final String PASSWORD = "password123";
    private static final String DEMO_SLUG = "test-petshop-moema";

    private final PetshopRepository petshopRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceItemRepository serviceRepository;
    private final ComboRepository comboRepository;
    private final TeamMemberRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public TestDataSeeder(PetshopRepository petshopRepository,
                          UserRepository userRepository,
                          ClientRepository clientRepository,
                          PetRepository petRepository,
                          AppointmentRepository appointmentRepository,
                          ServiceItemRepository serviceRepository,
                          ComboRepository comboRepository,
                          TeamMemberRepository teamRepository,
                          PasswordEncoder passwordEncoder,
                          TransactionTemplate transactionTemplate) {
        this.petshopRepository = petshopRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.comboRepository = comboRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = transactionTemplate;
    }

    @PostConstruct
    public void seed() {
        transactionTemplate.executeWithoutResult(status -> doSeed());
    }

    private void doSeed() {
        if (petshopRepository.findBySlug(DEMO_SLUG).isPresent()) {
            return;
        }

        LocalDate today = LocalDate.now();

        Petshop moema = createPetshop(
                "Test Petshop Moema", DEMO_SLUG,
                "contato@test-moema.local", "(11) 3022-4040",
                "Rua dos Pinheiros, 480 - São Paulo - SP", "12.345.678/0001-90");

        createUser(moema, "Marina Souza", "marina@petly.com.br", Role.ADMIN);
        createUser(moema, "Ana Martins", "ana@petly.com.br", Role.STAFF);

        createTeamMember(moema, "Ana Martins", "Groomer sênior", "Tosas especiais", "4,9", 7);
        createTeamMember(moema, "Lucas Silva", "Banhista", "Pets de grande porte", "4,8", 7);
        createTeamMember(moema, "Beatriz Rocha", "Groomer", "Filhotes e pets ansiosos", "5,0", 6);

        createService(moema, "Banho Essencial", "Banho, secagem, escovação e perfume", 60, 55, 70, 90);
        createService(moema, "Tosa na Máquina", "Tosa completa com acabamento", 90, 65, 85, 110);
        createService(moema, "Tosa Higiênica", "Patas, barriga e região íntima", 40, 35, 45, 60);
        createService(moema, "Hidratação", "Tratamento profundo para pelagem", 30, 30, 40, 55);

        createCombo(moema, "Banho Essencial", "Banho, secagem e perfume", 55, 120);
        createCombo(moema, "Combo Completo", "Banho, tosa e unhas", 95, 90);
        createCombo(moema, "Dia de Spa", "Hidratação e cuidados premium", 130, 60);

        Client clara = createClient(moema, "Clara Mendes", "clara.mendes@example.com", "(11) 98845-2031");
        Client rafael = createClient(moema, "Rafael Lima", "rafael.lima@example.com", "(11) 97732-8104");
        Client marina = createClient(moema, "Marina Alves", "marina.alves@example.com", "(11) 99610-3472");
        Client pedro = createClient(moema, "Pedro Costa", "pedro.costa@example.com", "(11) 98122-4430");
        Client livia = createClient(moema, "Lívia Freitas", "livia.freitas@example.com", "(11) 97446-0038");
        Client caio = createClient(moema, "Caio Nunes", "caio.nunes@example.com", "(11) 99870-1125");

        Pet bento = createPet(moema, clara, "Bento", "Cachorro", "Golden retriever", SizeTier.LARGE,
                LocalDate.of(2019, today.plusDays(2).getMonthValue(), today.plusDays(2).getDayOfMonth()));
        Pet pipoca = createPet(moema, rafael, "Pipoca", "Cachorro", "Shih-tzu", SizeTier.SMALL,
                LocalDate.of(2021, 3, 14));
        Pet thor = createPet(moema, marina, "Thor", "Cachorro", "Bulldog francês", SizeTier.MEDIUM,
                LocalDate.of(2020, 8, 22));
        Pet mel = createPet(moema, pedro, "Mel", "Cachorro", "Spitz alemão", SizeTier.SMALL,
                LocalDate.of(2022, today.plusDays(5).getMonthValue(), today.plusDays(5).getDayOfMonth()));
        Pet amora = createPet(moema, livia, "Amora", "Cachorro", "Poodle", SizeTier.MEDIUM,
                LocalDate.of(2018, 11, 2));
        Pet zeca = createPet(moema, caio, "Zeca", "Cachorro", "Vira-lata", SizeTier.LARGE,
                LocalDate.of(2017, 1, 30));

        createAppointment(moema, clara, bento, "Banho + Hidratação",
                today.atTime(9, 0), AppointmentStatus.IN_PROGRESS, "Ana Martins", money(70), 60);
        createAppointment(moema, rafael, pipoca, "Combo Completo",
                today.atTime(9, 0), AppointmentStatus.SCHEDULED, "Lucas Silva", money(95), 90);
        createAppointment(moema, marina, thor, "Banho especial",
                today.atTime(10, 0), AppointmentStatus.SCHEDULED, "Beatriz Rocha", money(85), 60);
        createAppointment(moema, pedro, mel, "Tosa higiênica",
                today.atTime(11, 30), AppointmentStatus.SCHEDULED, "Ana Martins", money(45), 40);
        createAppointment(moema, livia, amora, "Combo Spa",
                today.atTime(14, 0), AppointmentStatus.SCHEDULED, "Lucas Silva", money(130), 90);
        createAppointment(moema, caio, zeca, "Banho + Desembolo",
                today.atTime(15, 30), AppointmentStatus.SCHEDULED, "Beatriz Rocha", money(90), 60);

        seedHistory(moema,
                new Client[]{clara, rafael, marina, pedro, livia, caio},
                new Pet[]{bento, pipoca, thor, mel, amora, zeca},
                new String[]{"Ana Martins", "Lucas Silva", "Beatriz Rocha"});

        Petshop jardins = createPetshop(
                "Test Petshop Jardins", "test-petshop-jardins",
                "contato@test-jardins.local", "(11) 3033-5050",
                "Alameda dos Jardins, 120 - São Paulo - SP", "98.765.432/0001-10");

        createUser(jardins, "Carla Dias", "carla@petlyjardins.com.br", Role.ADMIN);

        createTeamMember(jardins, "Carla Dias", "Groomer", "Especialista em poodles", "4,9", 6);
        createService(jardins, "Banho Essencial", "Banho, secagem e perfume", 60, 50, 65, 85);
        createService(jardins, "Tosa na Tesoura", "Acabamento artesanal", 90, 70, 95, 120);
        createCombo(jardins, "Combo Completo", "Banho, tosa e unhas", 90, 40);

        Client beatriz = createClient(jardins, "Beatriz Nunes", "beatriz@example.com", "(11) 91111-2222");
        Client carlos = createClient(jardins, "Carlos Pereira", "carlos@example.com", "(11) 93333-4444");

        Pet nina = createPet(jardins, beatriz, "Nina", "Cachorro", "Poodle", SizeTier.SMALL,
                LocalDate.of(2021, today.plusDays(1).getMonthValue(), today.plusDays(1).getDayOfMonth()));
        Pet zecaJardins = createPet(jardins, carlos, "Zeca", "Cachorro", "Vira-lata", SizeTier.LARGE,
                LocalDate.of(2016, 6, 12));

        createAppointment(jardins, beatriz, nina, "Tosa na tesoura",
                today.atTime(9, 0), AppointmentStatus.SCHEDULED, "Carla Dias", money(70), 90);
        createAppointment(jardins, carlos, zecaJardins, "Banho + Desembolo",
                today.atTime(11, 0), AppointmentStatus.SCHEDULED, "Carla Dias", money(85), 60);
    }

    private void seedHistory(Petshop petshop, Client[] clients, Pet[] pets, String[] staff) {
        LocalDate today = LocalDate.now();
        String[] services = {"Banho Essencial", "Combo Completo", "Tosa na Máquina", "Dia de Spa", "Hidratação"};
        BigDecimal[] prices = {money(55), money(95), money(85), money(130), money(40)};

        int index = 0;
        for (int monthBack = 1; monthBack <= 6; monthBack++) {
            LocalDate base = today.minusMonths(monthBack).withDayOfMonth(12);
            int perMonth = 3 + (monthBack % 2);
            for (int i = 0; i < perMonth; i++) {
                createAppointment(petshop,
                        clients[index % clients.length],
                        pets[index % pets.length],
                        services[index % services.length],
                        base.atTime(9 + (i % 6), 0),
                        AppointmentStatus.COMPLETED,
                        staff[index % staff.length],
                        prices[index % prices.length],
                        60);
                index++;
            }
        }
    }

    private static BigDecimal money(int value) {
        return BigDecimal.valueOf(value).setScale(2);
    }

    private Petshop createPetshop(String name, String slug, String email, String phone,
                                  String address, String cnpj) {
        Petshop petshop = new Petshop();
        petshop.setName(name);
        petshop.setSlug(slug);
        petshop.setEmail(email);
        petshop.setPhone(phone);
        petshop.setAddress(address);
        petshop.setCnpj(cnpj);
        petshop.setActive(true);
        return petshopRepository.save(petshop);
    }

    private void createUser(Petshop petshop, String name, String email, Role role) {
        AppUser user = new AppUser();
        user.setPetshop(petshop);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }

    private void createTeamMember(Petshop petshop, String name, String role, String specialty,
                                  String rating, int dailyCapacity) {
        TeamMember member = new TeamMember();
        member.setPetshop(petshop);
        member.setName(name);
        member.setRole(role);
        member.setSpecialty(specialty);
        member.setRating(rating);
        member.setDailyCapacity(dailyCapacity);
        member.setActive(true);
        teamRepository.save(member);
    }

    private void createService(Petshop petshop, String name, String description, int duration,
                               int small, int medium, int large) {
        ServiceItem service = new ServiceItem();
        service.setPetshop(petshop);
        service.setName(name);
        service.setDescription(description);
        service.setDurationMinutes(duration);
        service.setPriceSmall(money(small));
        service.setPriceMedium(money(medium));
        service.setPriceLarge(money(large));
        serviceRepository.save(service);
    }

    private void createCombo(Petshop petshop, String name, String description,
                             int priceFrom, int popularity) {
        Combo combo = new Combo();
        combo.setPetshop(petshop);
        combo.setName(name);
        combo.setDescription(description);
        combo.setPriceFrom(money(priceFrom));
        combo.setPopularity(popularity);
        comboRepository.save(combo);
    }

    private Client createClient(Petshop petshop, String name, String email, String phone) {
        Client client = new Client();
        client.setPetshop(petshop);
        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);
        return clientRepository.save(client);
    }

    private Pet createPet(Petshop petshop, Client client, String name, String species,
                          String breed, SizeTier size, LocalDate birthDate) {
        Pet pet = new Pet();
        pet.setPetshop(petshop);
        pet.setClient(client);
        pet.setName(name);
        pet.setSpecies(species);
        pet.setBreed(breed);
        pet.setSize(size);
        pet.setBirthDate(birthDate);
        return petRepository.save(pet);
    }

    private void createAppointment(Petshop petshop, Client client, Pet pet, String service,
                                   java.time.LocalDateTime scheduledAt, AppointmentStatus status,
                                   String staffName, BigDecimal price, int durationMinutes) {
        Appointment appointment = new Appointment();
        appointment.setPetshop(petshop);
        appointment.setClient(client);
        appointment.setPet(pet);
        appointment.setService(service);
        appointment.setScheduledAt(scheduledAt);
        appointment.setStatus(status);
        appointment.setStaffName(staffName);
        appointment.setPrice(price);
        appointment.setDurationMinutes(durationMinutes);
        appointmentRepository.save(appointment);
    }
}
