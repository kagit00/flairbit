package com.dating.flairbit.utils.response;

import com.dating.flairbit.dto.*;
import com.dating.flairbit.dto.enums.MediaType;
import com.dating.flairbit.models.*;
import com.dating.flairbit.models.User;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class ResponseMakerUtility {

    private ResponseMakerUtility() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    public static ProfessionResponse buildProfession(Profession pr) {
        return ProfessionResponse.builder()
                .id(pr.getId())
                .company(pr.getCompany())
                .industry(pr.getIndustry())
                .jobTitle(pr.getJobTitle())
                .build();
    }

    public static EducationResponse buildEducation(Education ed) {
        return EducationResponse.builder()
                .degree(ed.getDegree()).id(ed.getId())
                .fieldOfStudy(ed.getFieldOfStudy())
                .graduationYear(ed.getGraduationYear())
                .institution(ed.getInstitution())
                .build();
    }

    public static LocationResponse buildLocation(Location loc) {
        return LocationResponse.builder()
                .city(loc.getCity()).id(loc.getId())
                .country(loc.getCountry())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
    }

    public static LifestyleResponse buildLifestyle(Lifestyle lifestyle) {
        return LifestyleResponse.builder()
                .drinks(lifestyle.getDrinks()).id(lifestyle.getId())
                .smokes(lifestyle.getSmokes())
                .religion(lifestyle.getReligion())
                .build();
    }

    public static PreferencesResponse buildPreferences(Preferences preferences) {
        return PreferencesResponse.builder()
                .preferredGenders(preferences.getPreferredGenders()).id(preferences.getId())
                .preferredMinAge(preferences.getPreferredMinAge())
                .preferredMaxAge(preferences.getPreferredMaxAge())
                .openToLongDistance(preferences.getOpenToLongDistance())
                .relationshipType(preferences.getRelationshipType())
                .wantsKids(preferences.getWantsKids())
                .build();
    }

    public static ProfileResponse getFullProfileResponse(Profile profile, String email) {
        List<MediaFileResponse> mediaFiles = new ArrayList<>();
        if (!profile.getMediaFiles().isEmpty()) {
            for (MediaFile mediaFile : profile.getMediaFiles()) {
                MediaFileResponse mediaFileResponse = getMediaFileResponse(mediaFile);
                mediaFiles.add(mediaFileResponse);
            }
        }

        return ProfileResponse.builder()
                .bio(profile.getBio()).userEmail(email).id(profile.getId())
                .displayName(profile.getDisplayName()).headline(profile.getHeadline())
                .profession(profile.getProfession() == null? ProfessionResponse.builder().build() : buildProfession(profile.getProfession()))
                .education(profile.getEducation() == null? EducationResponse.builder().build() : buildEducation(profile.getEducation()))
                .lifestyle(profile.getLifestyle() == null? LifestyleResponse.builder().build() : buildLifestyle(profile.getLifestyle()))
                .location(profile.getLocation() == null? LocationResponse.builder().build() : buildLocation(profile.getLocation()))
                .preferences(profile.getPreferences() == null? PreferencesResponse.builder().build() : buildPreferences(profile.getPreferences()))
                .mediaFiles(mediaFiles)
                .build();
    }

    public static ProfileResponse getBasicProfileResponse(Profile profile, String email) {
        return ProfileResponse.builder()
                .bio(profile.getBio()).userEmail(email)
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
