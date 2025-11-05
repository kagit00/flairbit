package com.dating.flairbit.service.chats;

import com.dating.flairbit.models.ChatSession;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.ChatSessionJDBCRepository;
import com.dating.flairbit.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionService {

    private final ChatSessionJDBCRepository chatSessionJdbcRepository;
    private final UserService userService;
    private final ProfileProcessor profileProcessor;

    public ChatSession getOrCreateSession(String fromEmail, String toEmail, String intent) {
        String a = canonicalEmail(fromEmail);
        String b = canonicalEmail(toEmail);
        if (a.equals(b)) {
            throw new IllegalArgumentException("Cannot create session with same email");
        }

        User userA = userService.getUserByEmail(a);
        User userB = userService.getUserByEmail(b);

        Profile pA = profileProcessor.getProfile(userA, intent);
        Profile pB = profileProcessor.getProfile(userB, intent);

        Profile profile1 = pA;
        Profile profile2 = pB;
        if (a.compareToIgnoreCase(b) > 0) {
            profile1 = pB;
            profile2 = pA;
        }

        ChatSession session = chatSessionJdbcRepository.getOrCreateSession(
                profile1.getId(), profile2.getId(), intent
        );

        session.setProfile1(profile1);
        session.setProfile2(profile2);

        return session;
    }

    private String canonicalEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}