package com.social.cyworld.service;

import com.social.cyworld.entity.Sign;
import com.social.cyworld.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class SignService {
    @Autowired
    SignRepository signRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp
    // ID 중복 확인
    public Sign findByUserId(String userId) {
        Sign sign = signRepository.findByUserId(userId);
        return sign;
    }

    // 플랫폼 + 이메일 확인 절차 (가입자와 비가입자 구분용) - kakao
    public Sign findByPlatformAndEmail(Sign sign) {
        Sign join = signRepository.findByPlatformAndEmail(sign.getPlatform(), sign.getEmail());
        return join;
    }

    // 플랫폼 + 휴대폰 확인 절차 (가입자와 비가입자 구분용) - naver
    public Sign findByPlatformAndPhoneNumber(Sign sign) {
        Sign join = signRepository.findByPlatformAndPhoneNumber(sign.getPlatform(), sign.getPhoneNumber());
        return join;
    }

    // 이메일 중복 확인
    public Sign findByEmail(String email) {
        Sign sign = signRepository.findByEmail(email);
        return sign;
    }

    // 휴대폰 중복 확인
    public Sign findByPhoneNumber(String phoneNumber) {
        Sign sign = signRepository.findByPhoneNumber(phoneNumber);
        return sign;
    }

    // 가입 성공시 고객 정보 저장
    public void signUp(Sign sign) {
        signRepository.save(sign);
    }

    // ID 찾기
    // 가입전 이름과 휴대폰 번호로 가입자 판단
    public Sign findByNameAndPhoneNumber(Sign sign) {
        Sign join = signRepository.findByNameAndPhoneNumber(sign.getName(), sign.getPhoneNumber());
        return join;
    }

    // PW 찾기
    public Sign findByUserIdAndNameAndEmail(Sign sign) {
        Sign join = signRepository.findByUserIdAndNameAndEmail(sign.getUserId(), sign.getName(), sign.getEmail());
        return join;
    }

    // 임시 PW 갱신
    public void updateSetInfoByUserId(HashMap<String, String> map) {
        signRepository.updateSetInfoByUserId(map.get("2"), map.get("1"));
    }

    // 로그인시 접속 날짜 기록
    public void updateTodayDate(Sign sign) {
        signRepository.updateSetToDateByIdx(sign.getToDate(), sign.getIdx());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Views
    // Idx 기준 회원정보 조회
    public Sign findByIdx(int idx) {
        Sign sign = signRepository.findByIdx(idx);
        return sign;
    }

    // 일일 조회수 증가
    public void updateSetTodayByIdx(Sign sign) {
        signRepository.updateSetTodayByIdx(sign.getToday(), sign.getIdx());
    }

    // 날짜가 바뀌며 총합 조회수 증가 및 일일 조회수 초기화
    public void updateSetTodayAndTotalAndToDateByIdx(Sign sign) {
        signRepository.updateSetTodayAndTotalAndToDateByIdx(sign.getToday(), sign.getTotal(), sign.getToDate(), sign.getIdx());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Ilchon
    public void updateSetIlchonByIdx(Sign sign) {
        signRepository.updateSetIlchonByIdx(sign.getIlchon(), sign.getIdx());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Search
    // 이름으로 친구 검색
    public List<Sign> findByNameContaining(String searchValue) {
        List<Sign> signList = signRepository.findByNameContaining(searchValue);
        return signList;
    }

    // ID로 친구 검색 - cyworld 가입자
    // email로 친구 검색 - 소셜 가입자
    public List<Sign> findByPlatformAndUserIdContainingOrPlatformInAndEmailContaining(String searchValue) {
        List<String> platforms = Arrays.asList("naver", "kakao");
        List<Sign> signList = signRepository.findByPlatformAndUserIdContainingOrPlatformInAndEmailContaining("cyworld", searchValue, platforms, searchValue);
        return signList;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Buy
    // 도토리 구매
    public void updateSetDotoryByIdx(Sign sign) {
        signRepository.updateSetDotoryByIdx(sign.getDotory(), sign.getIdx());
    }
}
