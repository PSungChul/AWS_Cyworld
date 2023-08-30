package com.social.cyworld.repository;

import com.social.cyworld.entity.UserMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserMainRepository extends JpaRepository<UserMain, Object> {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp
    // 유저 메인 정보 키에 해당하는 유저 메인 정보 조회
    UserMain findByIdx(String mUid);

    // 유저 idx에 해당하는 유저 메인 정보 조회
    @Query("SELECT um FROM UserMain um WHERE um.idx = (SELECT s.mUid FROM Sign s WHERE s.idx = :idx)")
    UserMain findBySignIdx(@Param("idx") int idx);

    // 로그인시 접속 날짜 기록
    @Query("UPDATE UserMain um SET um.toDate = :toDate WHERE um.idx= :mUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetToDateByIdx(@Param("toDate") String toDate, @Param("mUid") String mUid);

    // 유저 프로필 정보 삭제
    @Query("DELETE FROM UserMain um WHERE um.idx = :mUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByIdx(@Param("mUid") String mUid);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Views
    // 일일 조회수 증가
    @Query("UPDATE UserMain um SET um.today = :today WHERE um.idx = :mUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetTodayByIdx(@Param("today") int today, @Param("mUid") String mUid);

    // 날짜가 바뀌며 총합 조회수 증가 및 일일 조회수 초기화
    @Query("UPDATE UserMain um SET um.today = :today, um.total = :total, um.toDate = :toDate WHERE um.idx = :mUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetTodayAndTotalAndToDateByIdx(@Param("today") int today, @Param("total") int total, @Param("toDate") String toDate, @Param("mUid") String mUid);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Ilchon
    // 조회한 일촌수 갱신
    @Query("UPDATE UserMain um SET um.ilchon = :ilchon WHERE um.idx = (SELECT s.mUid FROM Sign s WHERE s.idx = :idx)")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetIlchonByIdx(@Param("ilchon") int ilchon, @Param("idx") int idx);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Main - Buy
    // 도토리 구매
    @Query("UPDATE UserMain um SET um.dotory = :dotory WHERE um.idx = :mUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetDotoryByIdx(@Param("dotory") int dotory, @Param("mUid") String mUid);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile
    // 프로필 미니미 변경
    @Query("UPDATE UserMain um SET um.minimi = :minimi WHERE um.idx = (SELECT s.mUid FROM Sign s WHERE s.idx = :idx)")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetMinimiByIdx(@Param("minimi") String minimi, @Param("idx") int idx);

    // 프로필 우측 - 메인 타이틀 수정
    @Query("UPDATE UserMain um SET um.mainTitle = :mainTitle WHERE um.idx = (SELECT s.mUid FROM Sign s WHERE s.idx = :idx)")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetMainTitleByIdx(@Param("mainTitle") String mainTitle, @Param("idx") int idx);

    // 프로필 좌측 - 메인 사진 및 메인 소개글 수정
    @Query("UPDATE UserMain um SET um.mainPhoto = :mainPhoto, um.mainText = :mainText WHERE um.idx = (SELECT s.mUid FROM Sign s WHERE s.idx = :idx)")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetMainPhotoAndMainTextByIdx(@Param("mainPhoto") String mainPhoto, @Param("mainText") String mainText, @Param("idx") int idx);
}
