package com.social.cyworld.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/*
에러 코드
0 : idx 추출 오류
-1 : 토큰 만료
-4 : 비회원 or 미니 홈피 주인 X
-99 : 다중 로그인
-100 : 리프레쉬 토큰 만료
*/

@Component
public class JwtUtil {
    @Autowired
    RedisUtil redisUtil;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 토큰 암호화키
    private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // idx에 해당하는 토큰 저장 map
    private Map<Integer, String> tokenMap = new ConcurrentHashMap<>();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 토큰 생성 - login
    public String createToken(int idx) {
        // 토큰 생성 전에 먼저 idx에 해당하는 토큰이 존재하는지 검증한다.
        validationCreateToken(idx);

        // AccessToken 발급
        // 현재 시간을 가져온다.
        Date now = new Date();
        // 토큰의 만료 시간을 계산한다.
        Calendar calendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
        // 현재 시간에서 1시간 후의 시간을 설정
        calendar.add(Calendar.MILLISECOND, 60 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
        Date expiryDate = calendar.getTime();
        String token = Jwts.builder()
                .setSubject(String.valueOf(idx))
                .setIssuedAt(now)
                .setExpiration(expiryDate) // setExpiration 매개변수가 Date로 되어있어 LocalDateTime를 사용하지 못한다.
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // 토큰 저장 map에 idx를 키로 토큰을 저장한다.
        tokenMap.put(idx, token);

        // RefreshToken 발급
        // 현재 시간을 가져온다.
        Date refreshNow = new Date();
        // 토큰의 만료 시간을 계산한다.
        Calendar refreshCalendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
        // 현재 시간에서 7일 후의 시간을 설정
        refreshCalendar.add(Calendar.MILLISECOND, 7 * 24 * 60 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
        Date refreshExpiryDate = refreshCalendar.getTime();
        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(idx))
                .setIssuedAt(refreshNow)
                .setExpiration(refreshExpiryDate) // setExpiration 매개변수가 Date로 되어있어 LocalDateTime를 사용하지 못한다.
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // Redis에 토큰을 키로 사용하여 리프레쉬 토큰을 저장한다.
        saveRefreshToken(token, refreshToken);

        // 토큰을 반환한다.
        return token;
    }
    // 토큰 생성 전 검증
    public void validationCreateToken(int idx) {
        if ( tokenMap.get(idx) != null ) {
            // idx에 해당하는 토큰을 가져온다.
            String token = tokenMap.get(idx);
            // 토큰을 파싱하여 토큰의 내용(Claims)을 가져온다.
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // 토큰의 만료 시간을 가져온다.
            Date expirationDate = claims.getExpiration();
            // 현재 시간을 가져온다.
            Date currentDate = new Date();
            // 토큰의 만료 시간과 현재 시간의 차이를 계산하여 남은 밀리초를 구한다.
            long remainingMillis = expirationDate.getTime() - currentDate.getTime();
            // 남은 밀리초를 분으로 변환하여 남은 분을 계산한다.
            int remainingMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(remainingMillis);
            // 무효화에 토큰을 추가하고, 토큰의 만료까지 남은 분을 지정하여 저장한다.
            redisUtil.setInvalidation("Invalidation " + token, "invalidationToken", remainingMinutes);
            // Redis에 무효화한 토큰을 키로 사용하여 리프레쉬 토큰을 삭제한다.
            redisUtil.delete(token);
            // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
            tokenMap.remove(idx);
        }
    }
    // 리프레쉬 토큰 저장
    public void saveRefreshToken(String token, String refreshToken) {
        // 리프레쉬 토큰을 파싱하여 리프레쉬 토큰의 내용(Claims)을 가져온다.
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        // 리프레쉬 토큰의 만료 시간을 가져온다.
        Date expirationDate = claims.getExpiration();
        // 현재 시간을 가져온다.
        Date currentDate = new Date();
        // 리프레쉬 토큰의 만료 시간과 현재 시간의 차이를 계산하여 남은 밀리초를 구한다.
        long remainingMillis = expirationDate.getTime() - currentDate.getTime();
        // 남은 밀리초를 분으로 변환하여 남은 분을 계산한다.
        int remainingMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(remainingMillis);
        // Redis에 토큰을 키로 사용하여 리프레쉬 토큰을 추가하고, 토큰의 만료까지 남은 분을 지정하여 저장한다.
        redisUtil.set(token, refreshToken, remainingMinutes);
    }

    // 토큰 검증
    public int validationToken(String token) throws JwtException {
        try {
            // 다중 로그인에 토큰이 존재하는지 체크
            if (validationInvalidationToken(token)) {
//                throw new RuntimeException("다중 로그인");
                return -99;
            }

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
    // 다중 로그인 토큰 검증
    public boolean validationInvalidationToken(String token) {
        // 검증 값
        boolean validation = true;
        // 무효화에 파라미터로 가져온 토큰이 존재하는지 체크한다.
        // 토큰이 존재하는 경우 - 무효화 만료 시간 이내
        if (redisUtil.hasKeyInvalidation("Invalidation " + token)) {
//                throw new RuntimeException("다중 로그인");
            // 검증 값을 반환한다.
            return validation;
        // 토큰이 존재하지 않는 경우 - 무효화 만료 시간 이후
        } else {
            // foreach로 토큰 저장 map을 돌린다.
            for ( Map.Entry<Integer, String> map : tokenMap.entrySet() ) {
                // 토큰 저장 map에 들어있는 토큰 값이 파라미터로 가져온 토큰과 일치할 경우
                if ( map.getValue().equals(token) ) {
                    // 검증 값을 false로 변경한다.
                    validation = false;
                    // for문을 빠져나간다.
                    break;
                }
            }
            // 검증 값을 반환한다.
            return validation;
        }
    }

    // 리프레쉬 토큰 검증 및 토큰 재생성
    public String validationRefreshToken(String token) {
        if (redisUtil.hasKey(token)) {
//                throw new RuntimeException("리프레쉬 토큰");
            String refreshToken = (String) redisUtil.get(token);
            // 리프레쉬 토큰을 파싱하여 리프레쉬 토큰의 내용(Claims)을 가져온다.
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            // 리프레쉬 토큰의 내용 중 사용자 idx 추출
            int idx = Integer.valueOf(claims.getSubject());
            // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
            tokenMap.remove(idx);

            // 토큰 재생성
            // 현재 시간을 가져온다.
            Date now = new Date();
            // 토큰의 만료 시간을 계산한다.
            Calendar calendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
            // 현재 시간에서 1시간 후의 시간을 설정
            calendar.add(Calendar.MILLISECOND, 60 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
            Date expiryDate = calendar.getTime();
            String newToken = Jwts.builder()
                    .setSubject(String.valueOf(idx))
                    .setIssuedAt(now)
                    .setExpiration(expiryDate) // setExpiration 매개변수가 Date로 되어있어 LocalDateTime를 사용하지 못한다.
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();

            // Redis에 키를 재생성한 토큰으로 갱신한다.
            redisUtil.update(token, newToken);
            // 토큰 저장 map에 idx를 키로 재생성 토큰을 저장한다.
            tokenMap.put(idx, newToken);

            // 재생성 토큰을 반환한다.
            return newToken;
        } else {
            return null;
        }
    }

    // 세션 만료로 인한 로그아웃
    public void timeoutToken(String token) {
        // 토큰을 파싱하여 토큰의 내용(Claims)을 가져온다.
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
        tokenMap.remove(Integer.valueOf(claims.getSubject()));
        // Redis에 토큰을 키로 사용하여 저장한 리프레쉬 토큰을 삭제한다.
        redisUtil.delete(token);
    }

    // 로그아웃 토큰
    public void logoutToken(String token) {
        // 토큰을 파싱하여 토큰의 내용(Claims)을 가져온다.
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
        tokenMap.remove(Integer.valueOf(claims.getSubject()));
        // 토큰의 만료 시간을 가져온다.
        Date expirationDate = claims.getExpiration();
        // 현재 시간을 가져온다.
        Date currentDate = new Date();
        // 토큰의 만료 시간과 현재 시간의 차이를 계산하여 남은 밀리초를 구한다.
        long remainingMillis = expirationDate.getTime() - currentDate.getTime();
        // 남은 밀리초를 분으로 변환하여 남은 분을 계산한다.
        int remainingMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(remainingMillis);
        // 블랙리스트에 토큰을 추가하고, 토큰의 만료까지 남은 분을 지정하여 저장한다.
        redisUtil.setBlackList("Logout " + token, "logoutToken", remainingMinutes);
    }
}