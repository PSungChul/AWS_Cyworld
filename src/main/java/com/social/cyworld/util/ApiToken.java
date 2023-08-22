package com.social.cyworld.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;

@Component
public class ApiToken {
    // HS256 알고리즘을 사용하여 랜덤한 시크릿 키를 생성한다.
    private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 토큰 생성
    public String createToken(int idx) {
        // API AccessToken 발급
        // 현재 시간을 가져온다.
        Date now = new Date();
        // 토큰의 만료 시간을 계산하기 위해 Calendar 클래스의 인스턴스를 생성한다.
        Calendar calendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
        // 현재 시간에서 5분 후의 밀리초로 토큰의 만료 시간을 계산하여 Calendar 클래스의 인스턴스에 추가한다.
        calendar.add(Calendar.MILLISECOND, 60 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
        // Calendar 클래스의 인스턴스에 추가된 만료 시간을 가져온다.
        Date expiryDate = calendar.getTime();
        // JWT 토큰을 생성한다.
        String token = Jwts.builder() // JWT 빌더를 생성한다.
                .setSubject(String.valueOf(idx)) // 토큰의 주제(subject)를 설정한다. - 로그인 유저 idx
                .setIssuedAt(now) // 토큰의 발행 시간을 설정한다.
                .setExpiration(expiryDate) // 토큰의 만료 시간을 설정한다. - 5분 후
                .signWith(secretKey, SignatureAlgorithm.HS256) // 생성한 시크릿 키와 HS256 알고리즘을 사용하여 JWT 토큰에 서명한다.
                .compact(); // 토큰을 생성하고 문자열 형태로 반환한다.

        // API AccessToken을 반환한다.
        return token;
    }

    // 토큰 검증
    public int validationToken(String token) throws JwtException {
        try {
            // 토큰을 파싱하여 토큰의 내용(Claims)을 가져온다.
            Claims claims = Jwts.parserBuilder() // JWT 파서 빌더를 생성한다.
                    .setSigningKey(secretKey) // 파싱에 사용할 시크릿 키를 설정한다.
                    .build() // JWT 파서를 빌드하여 생성한다.
                    .parseClaimsJws(token) // 가져온 토큰을 파싱하여 Jws 객체를 얻는다. (Jws - JSON Web Signature)
                    .getBody(); // 파싱된 토큰의 내용(Claims)을 가져온다. // 파싱된 토큰의 내용(Claims)을 가져온다.

            // 토큰의 내용 중 로그인 유저 idx를 추출한다.
            int idx = Integer.valueOf(claims.getSubject());

            // 추출한 로그인 유저 idx를 반환한다.
            return idx;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            System.out.println("만료된 JWT 토큰입니다.");
            // 에러 코드를 반환한다.
            return -1;
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT 토큰이 잘못되었습니다.");
        }
        // 추출 실패 할 경우
        // 에러 코드를 반환한다.
        return 0;
    }
}
