package com.social.cyworld.dto;

import com.social.cyworld.entity.Sign;
import com.social.cyworld.entity.UserLogin;
import com.social.cyworld.entity.UserMain;
import com.social.cyworld.entity.UserProfile;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {
    private Integer idx;
    private String lUid;
    private String pUid;
    private String mUid;
    private String email;
    private String emailKey;
    private String info;
    private String gender;
    private String name;
    private String birthday;
    private String phoneNumber;
    private String platform;
    private String roles;
    private String consent;
    private int dotory;
    private String minimi;
    private int ilchon;
    private String mainTitle;
    private String mainPhoto;
    private String mainText;
    private int today;
    private int total;
    private String toDate;

    // 싸이월드 가입자와 비가입자 구별
    public UserProfile toCyworldJoinCheck() {
        return UserProfile.builder()
                .email(email)
                .build();
    }

    // 네이버 가입자 비가입자 구별
    public UserProfile toNaverJoinCheck() {
        return UserProfile.builder()
                .name(name)
                .birthday(birthday)
                .phoneNumber(phoneNumber.replaceAll("-", "")) // 휴대폰 번호 하이픈 제거
                .build();
    }

    // ID 찾기
    public UserProfile toFindId() {
        return UserProfile.builder()
                .name(name)
                .phoneNumber(phoneNumber.replaceAll("-", "")) // 휴대폰 번호 하이픈 제거
                .build();
    }

    // PW 찾기
    public UserProfile toFindPw() {
        return UserProfile.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber.replaceAll("-", ""))
                .build();
    }

    // 로그인
    public UserLogin toLogin() {
        return UserLogin.builder()
                .userId(email)
                .info(info)
                .build();
    }

    // 회원가입
    public Sign toJoinSign(PasswordEncoder passwordEncoder) {
        return Sign.builder()
                .idx(null) // AUTO_INCREMENT로 null값 지정시 자동 인덱스 증가
                .mUid(UUID.randomUUID() + ":" + email + ":" + System.currentTimeMillis()) // UUID + 이메일 + 가입시간으로 중복성을 최소화하여 유저 메인 정보 키로 저장한다.
                .pUid(UUID.randomUUID() + ":" + email + ":" + System.currentTimeMillis()) // UUID + 이메일 + 가입시간으로 중복성을 최소화하여 유저 프로필 정보 키로 저장한다.
                .lUid(UUID.randomUUID() + ":" + email + ":" + System.currentTimeMillis()) // UUID + 이메일 + 가입시간으로 중복성을 최소화하여 유저 로그인 정보 키로 저장한다.
                .platform(platform)
                .roles(passwordEncoder.encode("USER")) // 권한을 "USER"로 설정하여 암호화한다.
                .consent(0) // API 동의 항목 체크에 미동의로 0을 저장한다.
                .build();
    }
    public UserLogin toJoinUserLogin(String lUid, PasswordEncoder passwordEncoder) {
        return UserLogin.builder()
                .idx(lUid)
                .userId(passwordEncoder.encode(email)) // 아이디에 이메일을 암호화하여 저장한다.
                .info(passwordEncoder.encode(info)) // 비밀번호에 이메일을 암호화하여 저장한다.
                .build();
    }
    public UserProfile toJoinUserProfile(String pUid) {
        return UserProfile.builder()
                .idx(pUid) // UUID + 이메일 + 가입시간으로 중복성 최소화
                .email(email)
                .gender(gender)
                .name(name)
                .birthday(birthday)
                .phoneNumber(phoneNumber.replaceAll("-", ""))
                .build();
    }
    public UserMain toJoinUserMain(String mUid) {
        // 접속 날짜에 가입 날짜를 기록하기 위해 Date 객체를 생성한다.
        Date date = new Date();
        // Date 객체를 원하는 형식대로 포맷한다.
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
        return UserMain.builder()
                .idx(mUid)
                .dotory(0) // 도토리 개수 지정
                .minimi("mainMinimi.png") // 기본 미니미 지정
                .ilchon(0) // 일촌 수 지정
                .mainTitle("안녕하세요~ " + name + " 님의 미니홈피입니다!") // 메인 상단 제목
                .mainPhoto("noImage") // 메인 프로필 사진 지정
                .mainText(name + "님의 미니홈피에 오신걸 환영합니다!") // 메인 프로필 소개글
                .today(0) // 일일 조회수
                .total(0) // 누적 조회수
                .toDate(today.format(date)) // 접속 날짜 ( 가입 날짜 )
                .build();
    }

    // 로그인 유저 정보
    public void toLoginUserDTO(Sign sign) {
        this.idx = sign.getIdx();
        this.platform = sign.getPlatform();
        this.roles = sign.getRoles();
    }

    // 도토리 팝업
    public void toDotoryPopupDTO(UserMain userMain, int idx) {
        this.idx = idx;
        this.dotory = userMain.getDotory();
    }

    // 미니미 변경
    public UserMain toUpdateMinimi() {
        return UserMain.builder()
                .minimi(minimi)
                .build();
    }

    // 프로필 우측 변경 - 비밀번호
    public UserLogin toRightInfo(PasswordEncoder passwordEncoder) {
        return UserLogin.builder()
                .info(passwordEncoder.encode(info)) // 새로운 비밀번호 암호화
                .build();
    }

    // 프로필 우측 변경 - 메인 타이틀
    public UserMain toRightMainTitle() {
        return UserMain.builder()
                .mainTitle(mainTitle)
                .build();
    }

    // 휴대폰 번호 수정
    public UserProfile toUpdatePhoneNumber() {
        return UserProfile.builder()
                .phoneNumber(phoneNumber.replaceAll("-", "")) // 휴대폰 번호 하이픈 제거
                .build();
    }
}
