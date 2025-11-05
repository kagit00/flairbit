package com.dating.flairbit.service.matchrequests;

import com.dating.flairbit.dto.enums.MatchRequestStatus;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.MatchRequest;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.MatchRequestRepository;
import com.dating.flairbit.service.GroupConfigService;
import com.dating.flairbit.service.user.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchRequestServiceImp implements MatchRequestService {
    private final UserService userService;
    private final ProfileProcessor profileProcessor;
    private final GroupConfigService groupConfigService;
    private final MatchRequestRepository matchRequestRepository;
    private final EntityManager entityManager;

    @Override
    @CacheEvict(value = {"matchRequestsCache"}, key = "#email + '_' + #intent + '_' + 'sent'")
    public void send(String from, String to, String intent) {
        User fromUser = userService.getUserByEmail(from);
        Profile fromProfile = profileProcessor.getProfile(fromUser, intent);
        Profile managedFrom = entityManager.getReference(Profile.class, fromProfile.getId());


        User toUser = userService.getUserByEmail(to);
        Profile toProfile = profileProcessor.getProfile(toUser, intent);
        Profile managedTo = entityManager.getReference(Profile.class, toProfile.getId());

        String groupId = groupConfigService.getGroupConfig(intent).getId();

        MatchRequest request = MatchRequest.builder()
                .from(managedFrom).to(managedTo).groupId(groupId)
                .build();

        matchRequestRepository.save(request);
    }

    @Override
    @CacheEvict(value = {"matchRequestsCache"}, key = "#email + '_' + #intent + '_' + 'received'")
    public void respond(String from, String to, String intent, MatchRequestStatus newStatus) {
        User fromUser = userService.getUserByEmail(from);
        Profile fromProfile = profileProcessor.getProfile(fromUser, intent);
        Profile managedFrom = entityManager.getReference(Profile.class, fromProfile.getId());

        User toUser = userService.getUserByEmail(to);
        Profile toProfile = profileProcessor.getProfile(toUser, intent);
        Profile managedTo = entityManager.getReference(Profile.class, toProfile.getId());

        MatchRequest request = matchRequestRepository.findByFromAndTo(managedTo, managedFrom).orElseThrow(
                () -> new BadRequestException("No match request exist b/w " + from + " and " + to)
        );

        request.setStatus(newStatus);
        matchRequestRepository.save(request);
    }

    @Override
    @Cacheable(value = "matchRequestsCache", key = "#email + '_' + #intent + '_' + 'sent'")
    public List<MatchRequest> getSentRequests(String email, String intent) {
        User fromUser = userService.getUserByEmail(email);
        Profile fromProfile = profileProcessor.getProfile(fromUser, intent);
        return matchRequestRepository.findByFrom(fromProfile);
    }

    @Override
    @Cacheable(value = "matchRequestsCache", key = "#email + '_' + #intent + '_' + 'received'")
    public List<MatchRequest> getReceivedRequests(String email, String intent) {
        User toUser = userService.getUserByEmail(email);
        Profile toProfile = profileProcessor.getProfile(toUser, intent);
        return matchRequestRepository.findByTo(toProfile);
    }
}
