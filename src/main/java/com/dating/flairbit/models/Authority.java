package com.dating.flairbit.models;

import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

/**
 * The type Authority.
 */
@ToString
public class Authority implements GrantedAuthority {
    private final String auth;

    /**
     * Instantiates a new Authority.
     *
     * @param authority the authority
     */
    public Authority(String authority) {
        this.auth = authority;
    }

    @Override
    public String getAuthority() {
        return auth;
    }
}
