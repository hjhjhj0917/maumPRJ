package com.example.maum.auth;

import com.example.maum.dto.UserInfoDTO;
import com.example.maum.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public record AuthInfo(UserInfoDTO userInfoDTO) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> pSet = new HashSet<>();

        String roles = CmmUtil.nvl(userInfoDTO.roles());

        if (!roles.isEmpty()) {
            for (String role : roles.split(",")) {
                pSet.add(new SimpleGrantedAuthority(role));
            }
        }

        return pSet;
    }

    @Override
    public String getUsername() {
        return CmmUtil.nvl(userInfoDTO.userId());
    }

    @Override
    public String getPassword() {
        return CmmUtil.nvl(userInfoDTO.password());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
