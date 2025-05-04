package com.dating.flairbit.service.auth;


import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;


@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User existingUser = this.userRepository.findByUsername(username);
        if (existingUser == null) {
            throw new NoSuchElementException("User not found");
        }
        return existingUser;
    }
}
