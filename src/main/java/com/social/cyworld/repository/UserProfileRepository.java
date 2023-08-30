package com.social.cyworld.repository;

import com.social.cyworld.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Object> {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp
    // 유저 프로필 정보 키에 해당하는 유저 프로필 정보 조회
    UserProfile findByIdx(String pUid);

    // 유저 idx에 해당하는 유저 프로필 정보 조회
    @Query("SELECT up FROM UserProfile up WHERE up.idx = (SELECT s.pUid FROM Sign s WHERE s.idx = :idx)")
    UserProfile findBySignIdx(@Param("idx") int idx);

    // 이름 + 생년월일 + 휴대폰 번호에 해당하는 유저 프로필 정보 조회
    UserProfile findByNameAndBirthdayAndPhoneNumber(String name, String birthday, String phoneNumber);

    // 이메일에 해당하는 유저 프로필 정보 조회
    UserProfile findByEmail(String email);

    // 휴대폰 번호에 해당하는 유저 프로필 정보 조회
    UserProfile findByPhoneNumber(String phoneNumber);

    // ID 찾기
    UserProfile findByNameAndPhoneNumber(String name, String phoneNumber);

    // PW 찾기
    UserProfile findByEmailAndNameAndPhoneNumber(String email, String name, String phoneNumber);

    // 유저 프로필 정보 삭제
    @Query("DELETE FROM UserProfile up WHERE up.idx = :pUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByIdx(@Param("pUid") String pUid);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile
    // 휴대폰 번호 변경
    @Query("UPDATE UserProfile up SET up.phoneNumber = :phoneNumber WHERE up.idx = (SELECT s.pUid FROM Sign s WHERE s.idx = :idx)")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetPhoneNumberByIdx(@Param("phoneNumber") String phoneNumber, @Param("idx") int idx);
}
