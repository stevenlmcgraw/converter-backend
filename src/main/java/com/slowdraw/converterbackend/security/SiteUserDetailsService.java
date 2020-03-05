package com.slowdraw.converterbackend.security;

import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.repository.RoleRepository;
import com.slowdraw.converterbackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder bCryptPasswordEncoder;

    public SiteUserDetailsService(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SiteUser user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found."));

        return UserPrincipal.createUserPrincipal(user);
    }
}
