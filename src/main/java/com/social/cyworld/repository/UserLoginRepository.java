package com.social.cyworld.repository;

import com.social.cyworld.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, Object> {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SignUp
    // 유저 로그인 정보 키에 해당하는 유저 로그인 정보 조회
    // 로그인
    UserLogin findByIdx(String lUid);

    // 유저 idx에 해당하는 유저 로그인 정보 조회
    @Query("SELECT ul FROM UserLogin ul WHERE ul.idx = (SELECT s.lUid FROM Sign s WHERE s.idx = :idx)")
    UserLogin findBySignIdx(@Param("idx") int idx);

    // 임시 PW 갱신
    @Query("UPDATE UserLogin ul SET ul.info = :info WHERE ul.idx = :lUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetInfoByEmail(@Param("info") String info, @Param("lUid") String lUid);

    // 유저 로그인 정보 삭제
    @Query("DELETE FROM UserLogin ul WHERE ul.idx = :lUid")
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByIdx(@Param("lUid") String lUid);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile
    // 프로필 우측 - 비밀번호 수정
    @Query("UPDATE UserLogin ul SET ul.info = :info WHERE ul.idx = (SELECT s.lUid FROM Sign s WHERE s.idx = :idx)")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetInfoByIdx(@Param("info") String info, @Param("idx") int idx);
}
