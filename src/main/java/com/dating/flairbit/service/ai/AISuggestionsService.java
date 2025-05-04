package com.dating.flairbit.service.ai;

import com.dating.flairbit.models.AISuggestion;

/**
 * The interface Ai suggestions service.
 */
public interface AISuggestionsService {
    /**
     * Generate suggestions ai suggestion.
     *
     * @param title       the title
     * @param sectionType the section type
     * @return the ai suggestion
     */
    AISuggestion generateSuggestions(String title, String sectionType);
}
