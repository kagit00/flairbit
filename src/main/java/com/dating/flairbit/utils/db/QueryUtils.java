package com.dating.flairbit.utils.db;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public final class QueryUtils {
    public static String getMatchSuggestionsTempTableSQL() {
        return "CREATE TEMP TABLE temp_match_suggestions (\n" +
                "    id UUID NOT NULL,\n" +
                "    group_id VARCHAR(50),\n" +
                "    participant_id VARCHAR(50),\n" +
                "    matched_participant_id VARCHAR(50),\n" +
                "    compatibility_score DOUBLE PRECISION,\n" +
                "    match_suggestion_type VARCHAR(50),\n" +
                "    created_at TIMESTAMP\n" +
                ") ON COMMIT DROP";
    }

    public static String getUpsertMatchSuggestionsSql() {
        return "INSERT INTO public.match_suggestions (\n" +
                "    id, group_id, participant_id, matched_participant_id, compatibility_score, match_suggestion_type, created_at\n" +
                ") SELECT DISTINCT ON (group_id, participant_id, matched_participant_id, match_suggestion_type)\n" +
                "    id, group_id, participant_id, matched_participant_id, compatibility_score, match_suggestion_type, created_at\n" +
                "FROM temp_match_suggestions\n" +
                "ORDER BY group_id, participant_id, matched_participant_id, match_suggestion_type, compatibility_score DESC, created_at DESC\n" +
                "ON CONFLICT (group_id, participant_id, matched_participant_id, match_suggestion_type)\n" +
                "DO UPDATE SET\n" +
                "    compatibility_score = EXCLUDED.compatibility_score,\n" +
                "    created_at = EXCLUDED.created_at";
    }
}
