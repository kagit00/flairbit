package com.dating.flairbit.utils.media.csv;

import com.dating.flairbit.dto.enums.NodeType;
import com.dating.flairbit.models.Education;
import com.dating.flairbit.models.Lifestyle;
import com.dating.flairbit.models.Location;
import com.dating.flairbit.models.Preferences;
import com.dating.flairbit.models.Profession;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.models.UserMatchState;
import com.dating.flairbit.utils.basic.BasicUtility;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;



public final class UserFieldsExtractor {

    private UserFieldsExtractor() {}

    public static List<UserView> extractMatchingProfileViews(User user) {
        if (user.getProfiles() == null || user.getProfiles().isEmpty()) return List.of();

        return user.getProfiles().stream()
                .filter(profile -> profile.getUserMatchState() != null && profile.getUserMatchState().isReadyForMatching())
                .map(profile -> new UserView(user, profile))
                .collect(Collectors.toList());
    }

    public static List<CsvExporter.FieldExtractor<UserView>> fieldExtractors() {
        return List.of(
                field("reference_id", v -> BasicUtility.safeExtract(v.user().getUsername())),
                field("name", v -> BasicUtility.safeExtract(v.profile(), Profile::getDisplayName)),
                field("gender", v -> BasicUtility.safeExtract(getMatchState(v), UserMatchState::getGender)),
                field("date_of_birth", v -> BasicUtility.safeExtract(getMatchState(v), UserMatchState::getDateOfBirth)),
                field("location", v -> BasicUtility.safeExtract(v.profile().getLocation(), Location::getCity)),
                field("intent", v -> BasicUtility.safeExtract(getMatchState(v), UserMatchState::getIntent)),
                field("bio", v -> BasicUtility.safeExtract(v.profile(), Profile::getBio)),
                field("smokes", v -> BasicUtility.safeExtract(v.profile().getLifestyle(), Lifestyle::getSmokes)),
                field("drinks", v -> BasicUtility.safeExtract(v.profile().getLifestyle(), Lifestyle::getDrinks)),
                field("wants_kids", v -> BasicUtility.safeExtract(v.profile().getPreferences(), Preferences::getWantsKids)),
                field("education", v -> BasicUtility.safeExtract(v.profile().getEducation(), Education::getFieldOfStudy)),
                field("religion", v -> BasicUtility.safeExtract(v.profile().getLifestyle(), Lifestyle::getReligion)),
                field("occupation", v -> BasicUtility.safeExtract(v.profile().getProfession(), Profession::getIndustry)),
                field("preferred_gender", v -> BasicUtility.safeExtract(v.profile().getPreferences(),
                        prefs -> prefs.getPreferredGenders() != null ? String.join(",", prefs.getPreferredGenders()) : "")),
                field("preferred_min_age", v -> BasicUtility.safeExtract(v.profile().getPreferences(), Preferences::getPreferredMinAge)),
                field("preferred_max_age", v -> BasicUtility.safeExtract(v.profile().getPreferences(), Preferences::getPreferredMaxAge)),
                field("relationship_type", v -> BasicUtility.safeExtract(v.profile().getPreferences(), Preferences::getRelationshipType)),
                field("open_to_long_distance", v -> BasicUtility.safeExtract(v.profile().getPreferences(), Preferences::getOpenToLongDistance)),
                field("ready_for_matching", v -> BasicUtility.safeExtract(getMatchState(v), UserMatchState::isReadyForMatching)),
                field("type", v -> NodeType.USER.name())
        );
    }

    private static UserMatchState getMatchState(UserView view) {
        return view.profile().getUserMatchState();
    }

    private static <T> CsvExporter.FieldExtractor<UserView> field(String header, Function<UserView, String> extractor) {
        return new CsvExporter.FieldExtractor<>() {
            @Override
            public String header() {
                return header;
            }

            @Override
            public String extract(UserView entity) {
                return extractor.apply(entity);
            }
        };
    }

    public record UserView(User user, Profile profile) {}
}
