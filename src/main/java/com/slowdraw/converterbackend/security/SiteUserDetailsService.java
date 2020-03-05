package com.slowdraw.converterbackend.security;

import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    public SiteUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SiteUser user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found."));

        return UserPrincipal.createUserPrincipal(user);
    }
}
