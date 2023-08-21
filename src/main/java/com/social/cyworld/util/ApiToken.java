package com.social.cyworld.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;

@Component
public class ApiToken {
    // 토큰 암호화키
    private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 토큰 생성 - login
    public String createToken(int idx) {
        // AccessToken 발급
        // 현재 시간을 가져온다.
        Date now = new Date();
        // 토큰의 만료 시간을 계산한다.
        Calendar calendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
        // 현재 시간에서 5분 후의 시간을 설정
        calendar.add(Calendar.MILLISECOND, 5 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
        Date expiryDate = calendar.getTime();
        String token = Jwts.builder()
                .setSubject(String.valueOf(idx))
                .setIssuedAt(now)
                .setExpiration(expiryDate) // setExpiration 매개변수가 Date로 되어있어 LocalDateTime를 사용하지 못한다.
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // 토큰을 반환한다.
        return token;
    }

    // 토큰 검증
    public int validationToken(String token) throws JwtException {
        try {
            // 토큰을 파싱하여 토큰의 내용(Claims)을 가져온다.
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 토큰의 내용 중 사용자 idx 추출
            int idx = Integer.valueOf(claims.getSubject());

            // 추출한 idx 반환
            return idx;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            System.out.println("만료된 JWT 토큰입니다.");
            return -1;
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT 토큰이 잘못되었습니다.");
        }
        // 추출 실패 할 경우
        return 0;
    }
}
