package com.social.cyworld.repository;

import com.social.cyworld.entity.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface SignRepository extends JpaRepository<Sign, Object> {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp
    // 로그인 유저 idx에 해당하는 유저 키 조회
    Sign findByIdx(int idx);

    // 이메일에 해당하는 유저 키 조회
    @Query("SELECT s FROM Sign s WHERE s.pUid = (SELECT up.idx FROM UserProfile up WHERE up.email = :email)")
    Sign findByEmail(@Param("email") String email);

    // 이름 + 생년월일 + 휴대폰 번호에 해당하는 유저 키 조회
    @Query("SELECT s FROM Sign s WHERE s.pUid = (SELECT up.idx FROM UserProfile up WHERE up.name = :name AND up.birthday = :birthday AND up.phoneNumber = :phoneNumber)")
    Sign findByNameAndBirthdayAndPhoneNumber(@Param("name") String name, @Param("birthday") String birthday, @Param("phoneNumber") String phoneNumber);

    // 유저 정보 삭제
    @Query("DELETE FROM Sign s WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByIdx(@Param("idx") int idx);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////API
    // API 로그인 유저 API 동의 항목 체크 완료
    @Query("UPDATE Sign s SET s.consent = 1 WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetConsentByIdx(@Param("idx") int idx);
}
