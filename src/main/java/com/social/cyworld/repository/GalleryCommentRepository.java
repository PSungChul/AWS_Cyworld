package com.social.cyworld.repository;

import com.social.cyworld.entity.GalleryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface GalleryCommentRepository extends JpaRepository<GalleryComment, Object> {
    // 댓글 전체 목록 조회
    List<GalleryComment> findByGalleryCommentIdxOrderByIdxDesc(int galleryCommentIdx);

    // 댓글 삭제 - 진짜 삭제가 아닌 삭제 표시로 업데이트
    @Query("UPDATE GalleryComment gc SET gc.galleryCommentDeleteCheck = :galleryCommentDeleteCheck WHERE gc.galleryCommentIdx = :galleryCommentIdx AND gc.galleryIdxComment = :galleryIdxComment AND gc.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetGalleryCommentDeleteCheckByGalleryCommentIdxAndGalleryIdxCommentAndIdx(@Param("galleryCommentDeleteCheck") int galleryCommentDeleteCheck, @Param("galleryCommentIdx") int galleryCommentIdx, @Param("galleryIdxComment") int galleryIdxComment, @Param("idx") int idx);
}