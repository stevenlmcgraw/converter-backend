package com.slowdraw.converterbackend.security;

import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.repository.RoleRepository;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteUserDetailsService implements UserDetailsService {

    private SiteUserRepository siteUserRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder bCryptPasswordEncoder;

    public SiteUserDetailsService(SiteUserRepository siteUserRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder bCryptPasswordEncoder) {
        this.siteUserRepository = siteUserRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SiteUser user = siteUserRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found."));

        return UserPrincipal.createUserPrincipal(user);
    }
}
