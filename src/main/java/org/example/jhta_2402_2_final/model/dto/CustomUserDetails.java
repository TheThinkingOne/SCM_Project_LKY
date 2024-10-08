package org.example.jhta_2402_2_final.model.dto;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.jhta_2402_2_final.model.dto.member.MemberDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Slf4j
public class CustomUserDetails implements UserDetails {

    private final MemberDto memberDto;

    public CustomUserDetails(MemberDto memberDto) {
        this.memberDto = memberDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberDto.getRoleId();
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return memberDto.getUserPassword();
    }

    @Override
    public String getUsername() {
        return memberDto.getUserName();
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