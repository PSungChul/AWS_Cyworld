package com.social.cyworld.service;

import com.social.cyworld.entity.Sign;
import com.social.cyworld.entity.UserLogin;
import com.social.cyworld.entity.UserMain;
import com.social.cyworld.entity.UserProfile;
import com.social.cyworld.repository.SignRepository;
import com.social.cyworld.repository.UserLoginRepository;
import com.social.cyworld.repository.UserMainRepository;
import com.social.cyworld.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class SignService {
    @Autowired
    SignRepository signRepository;
    @Autowired
    UserLoginRepository userLoginRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    UserMainRepository userMainRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp - Sign
    // 로그인 유저 idx에 해당하는 유저 키 조회
    public Sign findByIdx(int idx) {
        Sign sign = signRepository.findByIdx(idx);
        return sign;
    }

    // 이메일에 해당하는 유저 키 조회
    public Sign findByEmail(String email) {
        Sign sign = signRepository.findByEmail(email);
        return sign;
    }

    // 이름 + 생년월일 + 휴대폰 번호에 해당하는 유저 키 조회
    public Sign findByNameAndBirthdayAndPhoneNumber(String name, String birthday, String phoneNumber) {
        Sign sign = signRepository.findByNameAndBirthdayAndPhoneNumber(name, birthday, phoneNumber);
        return sign;
    }

    // 유저 정보 저장
    public Sign insertIntoSign(Sign sign) {
        Sign join = signRepository.save(sign);
        return join;
    }

    // 유저 정보 삭제
    public void deleteByIdx(int idx) {
        signRepository.deleteByIdx(idx);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp - UserLogin
    // 유저 로그인 정보 키에 해당하는 유저 로그인 정보 조회
    // 로그인
    public UserLogin findUserLoginByIdx(String lUid) {
        UserLogin userLogin = userLoginRepository.findByIdx(lUid);
        return userLogin;
    }

    // 유저 idx에 해당하는 유저 로그인 정보 조회
    public UserLogin findUserLoginBySignIdx(int idx) {
        UserLogin userLogin = userLoginRepository.findBySignIdx(idx);
        return userLogin;
    }

    // 임시 PW 갱신
    public void updateUserLoginSetInfoByEmail(HashMap<String, String> map) {
        userLoginRepository.updateSetInfoByEmail(map.get("2"), map.get("1"));
    }

    // 유저 로그인 정보 저장
    public UserLogin insertIntoUserLogin(UserLogin userLogin) {
        UserLogin join = userLoginRepository.save(userLogin);
        return join;
    }

    // 유저 로그인 정보 삭제
    public void deleteUserLoginByIdx(String lUid) {
        userLoginRepository.deleteByIdx(lUid);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp - UserProfile
    // 유저 프로필 정보 키에 해당하는 유저 프로필 정보 조회
    public UserProfile findUserProfileByIdx(String pUid) {
        UserProfile userProfile = userProfileRepository.findByIdx(pUid);
        return userProfile;
    }

    // 유저 idx에 해당하는 유저 프로필 정보 조회
    public UserProfile findUserProfileBySignIdx(int idx) {
        UserProfile userProfile = userProfileRepository.findBySignIdx(idx);
        return userProfile;
    }

    // 이름 + 생년월일 + 휴대폰 번호에 해당하는 유저 프로필 정보 조회
    public UserProfile findUserProfileByNameAndBirthdayAndPhoneNumber(String name, String birthday, String phoneNumber) {
        UserProfile sign = userProfileRepository.findByNameAndBirthdayAndPhoneNumber(name, birthday, phoneNumber);
        return sign;
    }

    // 이메일에 해당하는 유저 프로필 정보 조회
    public UserProfile findUserProfileByEmail(String email) {
        UserProfile sign = userProfileRepository.findByEmail(email);
        return sign;
    }

    // 휴대폰 번호에 해당하는 유저 프로필 정보 조회
    public UserProfile findUserProfileByPhoneNumber(String phoneNumber) {
        UserProfile sign = userProfileRepository.findByPhoneNumber(phoneNumber);
        return sign;
    }

    // ID 찾기
    public UserProfile findUserProfileByNameAndPhoneNumber(UserProfile userProfile) {
        UserProfile join = userProfileRepository.findByNameAndPhoneNumber(userProfile.getName(), userProfile.getPhoneNumber());
        return join;
    }

    // PW 찾기
    public UserProfile findUserProfileByEmailAndNameAndPhoneNumber(UserProfile userProfile) {
        UserProfile join = userProfileRepository.findByEmailAndNameAndPhoneNumber(userProfile.getEmail(), userProfile.getName(), userProfile.getPhoneNumber());
        return join;
    }

    // 유저 프로필 정보 저장
    public UserProfile insertIntoUserProfile(UserProfile userProfile) {
        UserProfile join = userProfileRepository.save(userProfile);
        return join;
    }

    // 유저 프로필 정보 삭제
    public void deleteUserProfileByIdx(String pUid) {
        userProfileRepository.deleteByIdx(pUid);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp - UserMain
    // 유저 메인 정보 키에 해당하는 유저 메인 정보 조회
    public UserMain findUserMainByIdx(String mUid) {
        UserMain userMain = userMainRepository.findByIdx(mUid);
        return userMain;
    }

    // 유저 idx에 해당하는 유저 메인 정보 조회
    public UserMain findUserMainBySignIdx(int idx) {
        UserMain userMain = userMainRepository.findBySignIdx(idx);
        return userMain;
    }

    // 로그인시 접속 날짜 기록
    public void updateUserMainSetToDateByIdx(UserMain userMain) {
        userMainRepository.updateSetToDateByIdx(userMain.getToDate(), userMain.getIdx());
    }

    // 유저 메인 정보 저장
    public UserMain insertIntoUserMain(UserMain userMain) {
        UserMain join = userMainRepository.save(userMain);
        return join;
    }

    // 유저 메인 정보 삭제
    public void deleteUserMainByIdx(String mUid) {
        userMainRepository.deleteByIdx(mUid);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Views
    // 일일 조회수 증가
    public void updateSetTodayByIdx(UserMain userMain) {
        userMainRepository.updateSetTodayByIdx(userMain.getToday(), userMain.getIdx());
    }

    // 날짜가 바뀌며 총합 조회수 증가 및 일일 조회수 초기화
    public void updateSetTodayAndTotalAndToDateByIdx(UserMain userMain) {
        userMainRepository.updateSetTodayAndTotalAndToDateByIdx(userMain.getToday(), userMain.getTotal(), userMain.getToDate(), userMain.getIdx());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Ilchon
    // 유저 idx에 해당하는 일촌 조회
    public void updateSetIlchonByIdx(int ilchon, int idx) {
        userMainRepository.updateSetIlchonByIdx(ilchon, idx);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Buy
    // 도토리 결제
    public void updateSetDotoryByIdx(UserMain userMain) {
        userMainRepository.updateSetDotoryByIdx(userMain.getDotory(), userMain.getIdx());
    }
}