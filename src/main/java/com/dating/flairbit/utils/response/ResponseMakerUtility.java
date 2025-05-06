package com.dating.flairbit.utils.response;

import com.dating.flairbit.dto.*;
import com.dating.flairbit.dto.enums.MediaType;
import com.dating.flairbit.models.*;
import com.dating.flairbit.models.User;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;

import java.util.HashMap;
import java.util.Map;


public final class ResponseMakerUtility {

    private ResponseMakerUtility() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    public static ProfessionResponse buildProfession(Profession pr) {
        return ProfessionResponse.builder()
                .company(pr.getCompany())
                .industry(pr.getIndustry())
                .jobTitle(pr.getJobTitle())
                .build();
    }

    public static EducationResponse buildEducation(Education ed) {
        return EducationResponse.builder()
                .degree(ed.getDegree())
                .fieldOfStudy(ed.getFieldOfStudy())
                .graduationYear(ed.getGraduationYear())
                .institution(ed.getInstitution())
                .build();
    }

    public static LocationResponse buildLocation(Location loc) {
        return LocationResponse.builder()
                .city(loc.getCity())
                .country(loc.getCountry())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
    }

    public static LifestyleResponse buildLifestyle(Lifestyle lifestyle) {
        return LifestyleResponse.builder()
                .drinks(lifestyle.getDrinks())
                .smokes(lifestyle.getSmokes())
                .religion(lifestyle.getReligion())
                .build();
    }

    public static PreferencesResponse buildPreferences(Preferences preferences) {
        return PreferencesResponse.builder()
                .preferredGenders(preferences.getPreferredGenders())
                .preferredMinAge(preferences.getPreferredMinAge())
                .preferredMaxAge(preferences.getPreferredMaxAge())
                .openToLongDistance(preferences.getOpenToLongDistance())
                .relationshipType(preferences.getRelationshipType())
                .wantsKids(preferences.getWantsKids())
                .build();
    }

    public static ProfileResponse getProfileResponse(Profile profile) {
        return ProfileResponse.builder()
                .bio(profile.getBio()).userEmail(profile.getUser().getEmail())
                .displayName(profile.getDisplayName()).headline(profile.getHeadline())
                .build();
    }


    public static MediaFileResponse getMediaFileResponse(MediaFile file) {
        return MediaFileResponse.builder()
                    .url(file.getFilePath())
                    .type(file.getFileType().startsWith("image") ? MediaType.IMAGE : MediaType.VIDEO)
                    .displayOrder(file.getDisplayOrder())
                    .uploadedAt(file.getUploadedAt())
                    .build();
    }

    public static UserMatchStateDTO buildMatchState(UserMatchState userMatchState) {
        return UserMatchStateDTO.builder()
                .dateOfBirth(userMatchState.getDateOfBirth()).lastMatchedAt(userMatchState.getLastMatchedAt())
                .gender(userMatchState.getGender()).readyForMatching(userMatchState.isReadyForMatching())
                .groupId(userMatchState.getGroupId()).sentToMatchingService(userMatchState.isSentToMatchingService())
                .intent(userMatchState.getIntent()).profileComplete(userMatchState.isProfileComplete())
                .build();
    }
}
