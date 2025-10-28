package com.dating.flairbit.processor;

import com.dating.flairbit.dto.*;
import com.dating.flairbit.dto.enums.GenderType;
import com.dating.flairbit.dto.enums.IntentType;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.service.auth.AuthenticationService;
import com.dating.flairbit.service.profile.ProfileService;
import com.dating.flairbit.service.profile.education.EducationUpdateService;
import com.dating.flairbit.service.profile.lifestyle.LifeStyleUpdateService;
import com.dating.flairbit.service.profile.location.LocationUpdateService;
import com.dating.flairbit.service.profile.prefrerences.PreferencesUpdateService;
import com.dating.flairbit.service.profile.profession.ProfessionUpdateService;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;


@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataSeeder implements ApplicationListener<ApplicationReadyEvent> {
    private final AuthenticationService authenticationService;
    private final ProfileService profileService;
    private final EducationUpdateService educationService;
    private final LifeStyleUpdateService lifestyleService;
    private final LocationUpdateService locationService;
    private final PreferencesUpdateService preferencesService;
    private final ProfessionUpdateService professionService;

    private static final int BATCH_SIZE = 1000;
    private static final int TOTAL_USERS = 20000;
    private static final int INIT = 0;
    private static final List<String> RELIGIONS = List.of(
            "Christianity", "Islam", "Hinduism", "Buddhism", "Judaism",
            "Sikhism", "Atheism", "Agnosticism", "Other"
    );

    private final Faker faker = new Faker();
    private final Random random = new Random();

    private static final String[] INTENTS = {
            IntentType.DATING.name(),
            IntentType.MARRIAGE.name()
    };

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //seedTestData();
    }

    private void seedTestData() {
        for (int i = INIT; i < INIT + TOTAL_USERS; i++) {
            try {
                seedSingleUser(i);
                if ((i + 1) % BATCH_SIZE == 0) {
                    log.info("Seeded {} users with complete profiles", i + 1);
                }
            } catch (Exception e) {
                log.warn("Failed to seed user #{}: {}", i + 1, e.getMessage(), e);
            }
        }
        log.info("Finished seeding {} test users", TOTAL_USERS);
    }

    private void seedSingleUser(int userIndex) {
        String email = "testuser+" + userIndex + "@example.com";

        // Step 1: Create user with OTP request
        authenticationService.requestOtp(
                RequestOtpRequest.builder()
                        .email(email)
                        .notificationEnabled(true)
                        .build()
        );

        // Step 2: Create a profile per intent (each with its own user match state)
        List<Profile> profiles = new ArrayList<>();
        for (String intent : INTENTS) {
            Profile profile = profileService.createOrUpdateProfile(email,
                    ProfileRequest.builder()
                            .displayName(faker.name().fullName())
                            .bio(faker.lorem().sentence())
                            .headline(faker.job().title())
                            .intent(intent)
                            .gender(getRandomGender())
                            .dob(getRandomDateOfBirth())
                            .build()
            );
            profiles.add(profile);
        }

        // Step 3: Add profile details after profile + match state creation
        for (Profile profile : profiles) {
            String intent = profile.getUserMatchState().getIntent();
            createEducation(profile.getUser().getEmail(), intent);
            createLifestyle(profile.getUser().getEmail(), intent);
            createLocation(profile.getUser().getEmail(), intent);
            createPreferences(profile.getUser().getEmail(), intent);
            createProfession(profile.getUser().getEmail(), intent);
        }
    }

    private void createEducation(String email, String intent) {
        educationService.createOrUpdateEducation(email,
                EducationRequest.builder()
                        .degree(faker.educator().course())
                        .fieldOfStudy(faker.educator().secondarySchool())
                        .institution(faker.university().name())
                        .graduationYear(faker.number().numberBetween(2000, 2023))
                        .build(),
                intent
        );
    }

    private void createLifestyle(String email, String intent) {
        lifestyleService.createOrUpdateLifestyle(email,
                LifestyleRequest.builder()
                        .drinks(faker.bool().bool())
                        .smokes(faker.bool().bool())
                        .religion(randomReligion())
                        .build(),
                intent
        );
    }

    private void createLocation(String email, String intent) {
        locationService.createOrUpdateLocation(email,
                LocationRequest.builder()
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .latitude(Double.parseDouble(faker.address().latitude()))
                        .longitude(Double.parseDouble(faker.address().longitude()))
                        .build(),
                intent
        );
    }

    private void createPreferences(String email, String intent) {
        preferencesService.createOrUpdatePreferences(email,
                PreferencesRequest.builder()
                        .preferredGenders(Set.of(GenderType.MALE.name(), GenderType.FEMALE.name()))
                        .preferredMinAge(faker.number().numberBetween(18, 25))
                        .preferredMaxAge(faker.number().numberBetween(26, 40))
                        .relationshipType("SERIOUS")
                        .wantsKids(faker.bool().bool())
                        .openToLongDistance(faker.bool().bool())
                        .build(),
                intent
        );
    }

    private void createProfession(String email, String intent) {
        professionService.createOrUpdateProfession(email,
                ProfessionRequest.builder()
                        .jobTitle(faker.job().title())
                        .company(faker.company().name())
                        .industry(faker.company().industry())
                        .build(),
                intent
        );
    }

    private String randomReligion() {
        return RELIGIONS.get(random.nextInt(RELIGIONS.size()));
    }

    private String getRandomGender() {
        String[] genders = {GenderType.FEMALE.name(), GenderType.MALE.name()};
        return genders[random.nextInt(genders.length)];
    }

    private LocalDate getRandomDateOfBirth() {
        int year = random.nextInt(40) + 1970;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        return LocalDate.of(year, month, day);
    }
}
