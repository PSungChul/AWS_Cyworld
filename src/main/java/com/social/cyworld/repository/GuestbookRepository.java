package com.social.cyworld.repository;

import com.social.cyworld.entity.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface GuestbookRepository extends JpaRepository<Guestbook, Object> {
    // 방명록 전체 조회 - 내림차순
    List<Guestbook> findByGuestbookIdxOrderByIdxDesc(int idx);

    // 방명록 삭제
    @Query("DELETE FROM Guestbook g WHERE g.guestbookIdx = :guestbookIdx AND g.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int deleteByGuestbookIdxAndIdx(@Param("guestbookIdx") int guestbookIdx, @Param("idx") int idx);

    // 수정을 위한 방명록 한 건 조회
    Guestbook findByGuestbookIdxAndIdx(int guestbookIdx, int idx);

    // 방명록 수정
    @Query("UPDATE Guestbook g SET g.guestbookName = :guestbookName, g.guestbookMinimi = :guestbookMinimi, g.guestbookRegDate = :guestbookRegDate, g.guestbookContent = :guestbookContent, g.guestbookSecretCheck = :guestbookSecretCheck WHERE g.guestbookIdx = :guestbookIdx AND g.guestbookSessionIdx = :guestbookSessionIdx AND g.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetGuestbookNameAndGuestbookMinimiAndGuestbookRegDateAndGuestbookContentAndGuestbookSecretCheckByGuestbookIdxAndGuestbookSessionIdxAndIdx(@Param("guestbookName") String guestbookName, @Param("guestbookMinimi") String guestbookMinimi, @Param("guestbookRegDate") String guestbookRegDate, @Param("guestbookContent") String guestbookContent, @Param("guestbookSecretCheck") int guestbookSecretCheck, @Param("guestbookIdx") int guestbookIdx, @Param("guestbookSessionIdx") int guestbookSessionIdx, @Param("idx") int idx);

    // 구해낸 방명록 좋아요 갯수를 보여주기 위해 컬럼에 작성하기
    @Query("UPDATE Guestbook g SET g.guestbookLikeNum = :guestbookLikeNum WHERE g.guestbookIdx = :guestbookIdx AND g.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetGuestbookLikeNumByGuestbookIdxAndIdx(@Param("guestbookLikeNum") int guestbookLikeNum, @Param("guestbookIdx") int guestbookIdx, @Param("idx") int idx);
}
