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
    // HS256 알고리즘을 사용하여 랜덤한 시크릿 키를 생성한다.
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
        // 토큰의 만료 시간을 계산하기 위해 Calendar 클래스의 인스턴스를 생성한다.
        Calendar calendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
        // 현재 시간에서 1시간 후의 밀리초로 토큰의 만료 시간을 계산하여 Calendar 클래스의 인스턴스에 추가한다.
        calendar.add(Calendar.MILLISECOND, 60 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
        // Calendar 클래스의 인스턴스에 추가된 만료 시간을 가져온다.
        Date expiryDate = calendar.getTime();
        // JWT 토큰을 생성한다.
        String token = Jwts.builder() // JWT 빌더를 생성한다.
                .setSubject(String.valueOf(idx)) // 토큰의 주제(subject)를 설정한다. - 로그인 유저 idx
                .setIssuedAt(now) // 토큰의 발행 시간을 설정한다.
                .setExpiration(expiryDate) // 토큰의 만료 시간을 설정한다. - 1시간 후
                .signWith(secretKey, SignatureAlgorithm.HS256) // 생성한 시크릿 키와 HS256 알고리즘을 사용하여 JWT 토큰에 서명한다.
                .compact(); // 토큰을 생성하고 문자열 형태로 반환한다.

        // 토큰 저장 map에 idx를 키로 토큰을 저장한다.
        tokenMap.put(idx, token);

        // RefreshToken 발급
        // 현재 시간을 가져온다.
        Date refreshNow = new Date();
        // 토큰의 만료 시간을 계산하기 위해 Calendar 클래스의 인스턴스를 생성한다.
        Calendar refreshCalendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
        // 현재 시간에서 7일 후의 밀리초로 토큰의 만료 시간을 계산하여 Calendar 클래스의 인스턴스에 추가한다.
        refreshCalendar.add(Calendar.MILLISECOND, 7 * 24 * 60 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
        // Calendar 클래스의 인스턴스에 추가된 만료 시간을 가져온다.
        Date refreshExpiryDate = refreshCalendar.getTime();
        // JWT 토큰을 생성한다.
        String refreshToken = Jwts.builder() // JWT 빌더를 생성한다.
                .setSubject(String.valueOf(idx)) // 토큰의 주제(subject)를 설정한다. - 로그인 유저 idx
                .setIssuedAt(refreshNow) // 토큰의 발행 시간을 설정한다.
                .setExpiration(refreshExpiryDate) // 토큰의 만료 시간을 설정한다. - 7일 후
                .signWith(secretKey, SignatureAlgorithm.HS256) // 생성한 시크릿 키와 HS256 알고리즘을 사용하여 JWT 토큰에 서명한다.
                .compact(); // 토큰을 생성하고 문자열 형태로 반환한다.

        // Redis에 토큰을 키로 사용하여 리프레쉬 토큰을 저장한다.
        saveRefreshToken(token, refreshToken);

        // 토큰을 반환한다.
        return token;
    }
    // 토큰 생성 전 검증
    public void validationCreateToken(int idx) {
        // 토큰 저장 map에 파라미터로 가져온 idx에 해당하는 토큰이 존재하는지 체크한다.
        // idx에 해당하는 토큰이 존재하는 경우
        if (tokenMap.get(idx) != null) {
            // idx에 해당하는 토큰을 가져온다.
            String token = tokenMap.get(idx);
            // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
            tokenMap.remove(idx);
            try {
                // 토큰을 파싱하여 토큰의 내용(Claims)을 가져온다.
                Claims claims = Jwts.parserBuilder() // JWT 파서 빌더를 생성한다.
                        .setSigningKey(secretKey) // 파싱에 사용할 시크릿 키를 설정한다.
                        .build() // JWT 파서를 빌드하여 생성한다.
                        .parseClaimsJws(token) // 가져온 토큰을 파싱하여 Jws 객체를 얻는다. (Jws - JSON Web Signature)
                        .getBody(); // 파싱된 토큰의 내용(Claims)을 가져온다.
                // 토큰의 만료 시간을 가져온다.
                Date expirationDate = claims.getExpiration();
                // 현재 시간을 가져온다.
                Date currentDate = new Date();
                // 토큰의 만료 시간과 현재 시간의 차이를 계산하여 남은 밀리초를 구한다.
                long remainingMillis = expirationDate.getTime() - currentDate.getTime();
                // Redis에서 무효화한 토큰을 키로 사용하여 리프레쉬 토큰을 삭제한다.
                redisUtil.delete(token);
                // 블랙리스트에 무효화한 토큰을 추가하고, 토큰의 만료까지 남은 밀리초를 지정하여 저장한다.
                redisUtil.setBlackList("Invalidation " + token, "invalidationToken", remainingMillis);
            } catch(io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
                System.out.println("잘못된 JWT 서명입니다.");
            } catch(ExpiredJwtException e){
                System.out.println("만료된 JWT 토큰입니다.");
                // Redis에서 무효화한 토큰을 키로 사용하여 리프레쉬 토큰을 삭제한다.
                redisUtil.delete(token);
            } catch(UnsupportedJwtException e){
                System.out.println("지원되지 않는 JWT 토큰입니다.");
            } catch(IllegalArgumentException e){
                System.out.println("JWT 토큰이 잘못되었습니다.");
            }
        }
    }
    // 리프레쉬 토큰 저장
    public void saveRefreshToken(String token, String refreshToken) {
        // 리프레쉬 토큰을 파싱하여 리프레쉬 토큰의 내용(Claims)을 가져온다.
        Claims claims = Jwts.parserBuilder() // JWT 파서 빌더를 생성한다.
                .setSigningKey(secretKey) // 파싱에 사용할 시크릿 키를 설정한다.
                .build() // JWT 파서를 빌드하여 생성한다.
                .parseClaimsJws(refreshToken) // 가져온 리프레쉬 토큰을 파싱하여 Jws 객체를 얻는다. (Jws - JSON Web Signature)
                .getBody(); // 파싱된 토큰의 내용(Claims)을 가져온다.
        // 리프레쉬 토큰의 만료 시간을 가져온다.
        Date expirationDate = claims.getExpiration();
        // 현재 시간을 가져온다.
        Date currentDate = new Date();
        // 리프레쉬 토큰의 만료 시간과 현재 시간의 차이를 계산하여 남은 밀리초를 구한다.
        long remainingMillis = expirationDate.getTime() - currentDate.getTime();
        // Redis에 토큰을 키로 사용하여 리프레쉬 토큰을 추가하고, 리프레쉬 토큰의 만료까지 남은 밀리초를 지정하여 저장한다.
        redisUtil.set(token, refreshToken, remainingMillis);
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
            Claims claims = Jwts.parserBuilder() // JWT 파서 빌더를 생성한다.
                    .setSigningKey(secretKey) // 파싱에 사용할 시크릿 키를 설정한다.
                    .build() // JWT 파서를 빌드하여 생성한다.
                    .parseClaimsJws(token) // 가져온 토큰을 파싱하여 Jws 객체를 얻는다. (Jws - JSON Web Signature)
                    .getBody(); // 파싱된 토큰의 내용(Claims)을 가져온다. // 파싱된 토큰의 내용(Claims)을 가져온다.

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
        // 블랙리스트에 파라미터로 가져온 토큰이 존재하는지 체크한다.
        // 토큰이 존재하는 경우 - 무효화 만료 시간 이내
        if (redisUtil.hasKeyBlackList("Invalidation " + token)) {
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
                    // foreach문을 빠져나간다.
                    break;
                }
            }
            // 검증 값을 반환한다.
            return validation;
        }
    }

    // 리프레쉬 토큰 검증 및 토큰 재생성
    public String validationRefreshToken(String token) {
        // Redis에 리프레쉬 토큰이 존재하는 경우 - 리프레쉬 토큰 유지
        if (redisUtil.hasKey(token)) {
//                throw new RuntimeException("리프레쉬 토큰");
            // Redis에서 토큰을 키로 사용하여 리프레쉬 토큰을 가져온다.
            String refreshToken = (String) redisUtil.get(token);
            // 리프레쉬 토큰을 파싱하여 리프레쉬 토큰의 내용(Claims)을 가져온다.
            Claims claims = Jwts.parserBuilder() // JWT 파서 빌더를 생성한다.
                    .setSigningKey(secretKey) // 파싱에 사용할 시크릿 키를 설정한다.
                    .build() // JWT 파서를 빌드하여 생성한다.
                    .parseClaimsJws(refreshToken) // 가져온 리프레쉬 토큰을 파싱하여 Jws 객체를 얻는다. (Jws - JSON Web Signature)
                    .getBody(); // 파싱된 토큰의 내용(Claims)을 가져온다. // 파싱된 토큰의 내용(Claims)을 가져온다.

            // 리프레쉬 토큰의 내용 중 사용자 idx 추출
            int idx = Integer.valueOf(claims.getSubject());
            // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
            tokenMap.remove(idx);

            // 토큰 재생성
            // 현재 시간을 가져온다.
            Date now = new Date();
            // 토큰의 만료 시간을 계산하기 위해 Calendar 클래스의 인스턴스를 생성한다.
            Calendar calendar = Calendar.getInstance(); // Calendar 클래스의 인스턴스 반환 // Calendar cal = new Calendar(); - 애러, 추상클래스는 인스턴스 생성 불가
            // 현재 시간에서 1시간 후의 밀리초로 토큰의 만료 시간을 계산하여 Calendar 클래스의 인스턴스에 추가한다.
            calendar.add(Calendar.MILLISECOND, 60 * 60 * 1000); // Calendar.MILLISECOND - 1000분의 1초(0~999)
            // Calendar 클래스의 인스턴스에 추가된 만료 시간을 가져온다.
            Date expiryDate = calendar.getTime();
            // JWT 토큰을 재생성한다.
            String newToken = Jwts.builder() // JWT 빌더를 생성한다.
                    .setSubject(String.valueOf(idx)) // 토큰의 주제(subject)를 설정한다. - 로그인 유저 idx
                    .setIssuedAt(now) // 토큰의 발행 시간을 설정한다.
                    .setExpiration(expiryDate) // 토큰의 만료 시간을 설정한다. - 1시간 후
                    .signWith(secretKey, SignatureAlgorithm.HS256) // 생성한 시크릿 키와 HS256 알고리즘을 사용하여 JWT 토큰에 서명한다.
                    .compact(); // 토큰을 생성하고 문자열 형태로 반환한다.

            // Redis에 저장된 리프레쉬 토큰의 키를 재생성한 토큰으로 갱신한다.
            redisUtil.update(token, newToken);
            // 토큰 저장 map에 idx를 키로 재생성 토큰을 저장한다.
            tokenMap.put(idx, newToken);

            // 재생성 토큰을 반환한다.
            return newToken;
        // Redis에 리프레쉬 토큰이 존재하지 않는 경우 - 리프레쉬 토큰 만료
        } else {
            // null을 반환한다.
            return null;
        }
    }

    // 리프레쉬 토큰의 만료까지 남은 시간
    public int refreshTokenExpiryDate(String token) {
        // Redis에 리프레쉬 토큰이 존재하는 경우 - 리프레쉬 토큰 유지
        if (redisUtil.hasKey(token)) {
//                throw new RuntimeException("리프레쉬 토큰");
            // Redis에서 토큰을 키로 사용하여 리프레쉬 토큰을 가져온다.
            String refreshToken = (String) redisUtil.get(token);
            // 리프레쉬 토큰을 파싱하여 리프레쉬 토큰의 내용(Claims)을 가져온다.
            Claims claims = Jwts.parserBuilder() // JWT 파서 빌더를 생성한다.
                    .setSigningKey(secretKey) // 파싱에 사용할 시크릿 키를 설정한다.
                    .build() // JWT 파서를 빌드하여 생성한다.
                    .parseClaimsJws(refreshToken) // 가져온 리프레쉬 토큰을 파싱하여 Jws 객체를 얻는다. (Jws - JSON Web Signature)
                    .getBody(); // 파싱된 토큰의 내용(Claims)을 가져온다.

            // 리프레쉬 토큰의 만료 시간을 가져온다.
            Date expirationDate = claims.getExpiration();
            // 현재 시간을 가져온다.
            Date currentDate = new Date();
            // 리프레쉬 토큰의 만료 시간과 현재 시간의 차이를 계산하여 남은 밀리초를 구한다.
            long remainingMillis = expirationDate.getTime() - currentDate.getTime();
            // 밀리초를 초로 변환하여 남은 시간을 초 단위로 계산한다.
            int remainingSeconds = (int) (remainingMillis / 1000);
            // 리프레쉬 토큰의 만료까지 남은 초를 반환한다.
            return remainingSeconds;
        // Redis에 리프레쉬 토큰이 존재하지 않는 경우 - 리프레쉬 토큰 만료
        } else {
            // 0을 반환한다.
            return 0;
        }
    }

    // 로그아웃 토큰
    public void logoutToken(String token) {
        try {
            // 토큰을 파싱하여 토큰의 내용(Claims)을 가져온다.
            Claims claims = Jwts.parserBuilder() // JWT 파서 빌더를 생성한다.
                    .setSigningKey(secretKey) // 파싱에 사용할 시크릿 키를 설정한다.
                    .build() // JWT 파서를 빌드하여 생성한다.
                    .parseClaimsJws(token) // 가져온 토큰을 파싱하여 Jws 객체를 얻는다. (Jws - JSON Web Signature)
                    .getBody(); // 파싱된 토큰의 내용(Claims)을 가져온다.
            // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
            tokenMap.remove(Integer.valueOf(claims.getSubject()));
            // 토큰의 만료 시간을 가져온다.
            Date expirationDate = claims.getExpiration();
            // 현재 시간을 가져온다.
            Date currentDate = new Date();
            // 토큰의 만료 시간과 현재 시간의 차이를 계산하여 남은 밀리초를 구한다.
            long remainingMillis = expirationDate.getTime() - currentDate.getTime();
            // Redis에 로그아웃한 토큰을 키로 사용하여 리프레쉬 토큰을 삭제한다.
            redisUtil.delete(token);
            // 블랙리스트에 로그아웃한 토큰을 추가하고, 토큰의 만료까지 남은 밀리초를 지정하여 저장한다.
            redisUtil.setBlackList("Logout " + token, "logoutToken", remainingMillis);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            System.out.println("만료된 JWT 토큰입니다.");
            // 세션 만료로 인한 로그아웃
            // Redis에서 로그아웃한 토큰을 키로 사용하여 리프레쉬 토큰을 삭제한다.
            redisUtil.delete(token);
            // 토큰 저장 map에서 idx에 해당하는 토큰을 삭제한다.
            // foreach로 토큰 저장 map을 돌린다.
            for ( Map.Entry<Integer, String> map : tokenMap.entrySet() ) {
                // 토큰 저장 map에 들어있는 토큰 값이 파라미터로 가져온 토큰과 일치할 경우
                if ( map.getValue().equals(token) ) {
                    // 토큰 저장 map에서 찾아낸 값에 해당하는 키로 로그아웃된 토큰을 삭제한다.
                    tokenMap.remove(map.getKey());
                    // foreach문을 빠져나간다.
                    break;
                }
            }
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT 토큰이 잘못되었습니다.");
        }
    }
}