package com.social.cyworld.repository;

import com.social.cyworld.entity.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.transaction.Transactional;
import java.util.List;

@ResponseBody
public interface SignRepository extends JpaRepository<Sign, Object> {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp
    // 가입자 조회
    Sign findByNameAndBirthdayAndPhoneNumber(String name, String birthday, String phoneNumber);

    // 이메일 중복 확인
    Sign findByEmail(String email);

    // 휴대폰 중복 확인
    Sign findByPhoneNumber(String phoneNumber);

    // ID 찾기
    // 가입전 가입자인지 체크
    Sign findByNameAndPhoneNumber(String name, String phoneNumber);

    // PW 찾기
    Sign findByEmailAndNameAndPhoneNumber(String email, String name, String phoneNumber);

    // 임시 PW 갱신
    @Query("UPDATE Sign s SET s.info = :info WHERE s.email = :email")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetInfoByEmail(@Param("info") String info, @Param("email") String email);

    // 로그인시 접속 날짜 기록
    @Query("UPDATE Sign s SET s.toDate = :toDate WHERE s.idx= :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetToDateByIdx(@Param("toDate") String toDate, @Param("idx") int idx);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Views
    // Idx 기준 유저정보 조회
    Sign findByIdx(int idx);

    // 일일 조회수 증가
    @Query("UPDATE Sign s SET s.today = :today WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetTodayByIdx(@Param("today") int today, @Param("idx") int idx);

    // 날짜가 바뀌며 총합 조회수 증가 및 일일 조회수 초기화
    @Query("UPDATE Sign s SET s.today = :today, s.total = :total, s.toDate = :toDate WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetTodayAndTotalAndToDateByIdx(@Param("today") int today, @Param("total") int total, @Param("toDate") String toDate, @Param("idx") int idx);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Ilchon
    // 조회된 일촌수 갱신
    @Query("UPDATE Sign s SET s.ilchon = :ilchon WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetIlchonByIdx(@Param("ilchon") int ilchon, @Param("idx") int idx);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Search
    // 이름으로 친구 검색
    List<Sign> findByNameContaining(String searchValue);

	// email로 친구 검색
    List<Sign> findByEmailContaining(String searchValue);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Buy
    // 도토리 구매
    @Query("UPDATE Sign s SET s.dotory = :dotory WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetDotoryByIdx(@Param("dotory") int dotory, @Param("idx") int idx);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile
    // 프로필 미니미 변경
    @Query("UPDATE Sign s SET s.minimi = :minimi WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetMinimiByIdx(@Param("minimi") String minimi, @Param("idx") int idx);

    // 프로필 좌측 - 메인 사진 및 메인 소개글 수정
    @Query("UPDATE Sign s SET s.mainPhoto = :mainPhoto, s.mainText = :mainText WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetMainPhotoAndMainTextByIdx(@Param("mainPhoto") String mainPhoto, @Param("mainText") String mainText, @Param("idx") int idx);

    // 프로필 우측 (cyworld 가입자) - 메인 타이틀 및 비밀번호 수정
    @Query("UPDATE Sign s SET s.info = :info, s.mainTitle = :mainTitle WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetInfoAndMainTitleByIdx(@Param("info") String info, @Param("mainTitle") String mainTitle, @Param("idx") int idx);

    // 프로필 우측 (소셜  가입자) - 비밀번호 없이 메인 타이틀만 수정
    @Query("UPDATE Sign s SET s.mainTitle = :mainTitle WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetMainTitleByIdx(@Param("mainTitle") String mainTitle, @Param("idx") int idx);

    // 휴대폰 번호 변경
    @Query("UPDATE Sign s SET s.phoneNumber = :phoneNumber WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetPhoneNumberByIdx(@Param("phoneNumber") String phoneNumber, @Param("idx") int idx);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////API
    // API 로그인 유저 API 동의 항목 체크 완료
    @Query("UPDATE Sign s SET s.consent = 1 WHERE s.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetConsentByIdx(@Param("idx") int idx);
}
