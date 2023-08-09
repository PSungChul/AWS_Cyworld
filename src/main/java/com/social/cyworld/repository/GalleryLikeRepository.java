package com.social.cyworld.repository;

import com.social.cyworld.entity.GalleryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface GalleryLikeRepository extends JpaRepository<GalleryLike, Object> {
    // 좋아요를 이미 눌렀는지 확인하기 위한 작업
    GalleryLike findByGalleryLikeIdxAndGalleryLikeRefAndGalleryLikeSessionIdx(int galleryLikeIdx, int galleryLikeRef, int galleryLikeSessionIdx);

    // 게시글 좋아요 개수 구하기
    int countByGalleryLikeIdxAndGalleryLikeRef(int galleryLikeIdx, int galleryLikeRef);

    // 게시글 좋아요 취소
    @Query("DELETE FROM GalleryLike gl WHERE gl.galleryLikeIdx = :galleryLikeIdx AND gl.galleryLikeRef = :galleryLikeRef AND gl.galleryLikeSessionIdx = :galleryLikeSessionIdx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByGalleryLikeIdxAndGalleryIdxLikeAndGalleryLikeSessionIdx(@Param("galleryLikeIdx") int galleryLikeIdx, @Param("galleryLikeRef") int galleryLikeRef, @Param("galleryLikeSessionIdx") int galleryLikeSessionIdx);
}