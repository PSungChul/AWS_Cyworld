package com.social.cyworld.repository;

import com.social.cyworld.entity.GuestbookLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface GuestbookLikeRepository extends JpaRepository<GuestbookLike, Object> {
    // 좋아요를 이미 눌렀는지 확인하기 위한 조회
    GuestbookLike findByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(int guestbookLikeIdx, int guestbookLikeRef, int guestbookLikeSessionIdx);

    // 방명록 좋아요 갯수 구하기
    int countByGuestbookLikeIdxAndGuestbookLikeRef(int guestbookLikeIdx, int guestbookLikeRef);

    // 방명록 좋아요 취소
    @Query("DELETE FROM GuestbookLike g WHERE g.guestbookLikeIdx = :guestbookLikeIdx AND g.guestbookLikeRef = :guestbookLikeRef AND g.guestbookLikeSessionIdx = :guestbookLikeSessionIdx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(@Param("guestbookLikeIdx") int guestbookLikeIdx, @Param("guestbookLikeRef") int guestbookLikeRef, @Param("guestbookLikeSessionIdx") int guestbookLikeSessionIdx);
}
