package com.social.cyworld.repository;

import com.social.cyworld.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Object> {
    // 다이어리 전체 목록 조회 - 내림차순
    List<Diary> findByDiaryIdxOrderByIdxDesc(int idx);

    // 다이어리 글 삭제
    @Query("DELETE FROM Diary d WHERE d.diaryIdx = :diaryIdx AND d.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int deleteByDiaryIdxAndIdx(@Param("diaryIdx") int diaryIdx, @Param("idx") int idx);

    // 수정을 위한 다이어리 글 한 건 조회
    Diary findByDiaryIdxAndIdx(int diaryIdx, int idx);

    // 다이어리 글 수정
    @Query("UPDATE Diary d SET d.diaryContent = :diaryContent, d.diaryRegDate = :diaryRegDate WHERE d.diaryIdx = :diaryIdx AND d.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetDiaryContentAndDiaryRegDateByDiaryIdxAndIdx(@Param("diaryContent") String diaryContent, @Param("diaryRegDate") String diaryRegDate, @Param("diaryIdx") int diaryIdx, @Param("idx") int idx);
}