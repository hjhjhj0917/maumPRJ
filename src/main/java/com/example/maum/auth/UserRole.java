package com.example.maum.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {

    // users.role 에 저장되는 값과 같음. 스프링 권한 이름은 앞에 ROLE_ 을 붙여 만듦
    ADMIN("ADMIN"),
    USER("USER");

    private final String value;
}
