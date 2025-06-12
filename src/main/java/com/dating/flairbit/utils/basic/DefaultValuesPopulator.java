package com.dating.flairbit.utils.basic;

import com.dating.flairbit.models.Role;
import com.dating.flairbit.repo.RoleRepository;
import com.dating.flairbit.utils.Constant;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for populating default values such as roles and generating IDs or timestamps.
 * This class is not meant to be instantiated.
 */
@Slf4j
public final class DefaultValuesPopulator {

    private DefaultValuesPopulator() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    public static LocalDateTime getCurrentTimestamp() {
        return LocalDateTime.now();
    }

    /**
     * Generates a random UUID string.
     *
     * @return a new UUID string
     */
    public static String getUid() {
        return UUID.randomUUID().toString();
    }


    public static Role populateDefaultUserRoles(RoleRepository roleRepository) {
        return populateRoleIfAbsent(Constant.USER, roleRepository);
    }

    private static Role populateRoleIfAbsent(String roleName, RoleRepository roleRepository) {
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            log.info("{} role not found. Creating it.", roleName);
            role = roleRepository.save(Role.builder().name(roleName).build());
        }
        return role;
    }

    public static UUID getUid2() {
        return UUID.randomUUID();
    }

}
