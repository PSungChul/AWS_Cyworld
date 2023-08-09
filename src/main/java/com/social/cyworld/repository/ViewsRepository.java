package com.social.cyworld.repository;

import com.social.cyworld.entity.Views;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ViewsRepository extends JpaRepository<Views, Object> {
    // 로그인한 유저가 해당 미니홈피로 방문한 기록 조회
    Views findByViewsIdxAndViewsSessionIdx(int viewsIdx, int viewsSessionIdx);

    // 방문 기록이 있을 경우 날짜 비교 후 날짜가 다르면 해당 날짜에 현재 날짜로 갱신
    @Query("UPDATE Views v SET v.todayDate = :todayDate WHERE v.viewsIdx = :viewsIdx AND v.viewsSessionIdx = :viewsSessionIdx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetTodayDateByViewsIdxAndViewsSessionIdx(@Param("todayDate") String todayDate, @Param("viewsIdx") int viewsIdx, @Param("viewsSessionIdx") int viewsSessionIdx);
}
