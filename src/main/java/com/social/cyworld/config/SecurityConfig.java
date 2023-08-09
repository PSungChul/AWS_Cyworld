package com.social.cyworld.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    // 비밀번호 암호화 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder - BCrypt 해싱 함수(BCrypt hashing function)를 사용해서 비밀번호를 인코딩해주는 메소드와
        //                         로그인 진행중인 유저에 의해 제출된 비밀번호와 저장소에 저장되어 있는 비밀번호의 일치 여부를 확인해주는 메소드를 제공한다.
        //                         PasswordEncoder 인터페이스를 구현한 클래스이다.
        return new BCryptPasswordEncoder();
    }
}
