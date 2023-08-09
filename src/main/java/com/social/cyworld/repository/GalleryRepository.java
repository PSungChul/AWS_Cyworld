package com.social.cyworld.repository;

import com.social.cyworld.entity.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Object> {
    // 사진첩 전체 목록 조회 - 내림차순
    List<Gallery> findByGalleryIdxOrderByIdxDesc(int idx);

    // 게시글 삭제
    @Query("DELETE FROM Gallery g WHERE g.galleryIdx = :galleryIdx AND g.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int deleteByGalleryIdxAndIdx(@Param("galleryIdx") int galleryIdx, @Param("idx") int idx);

    // 수정을 위한 게시글 한 건 조회
    Gallery findByGalleryIdxAndIdx(int galleryIdx, int idx);

    // 게시글 수정
    @Query("UPDATE Gallery g SET g.galleryTitle = :galleryTitle, g.galleryRegDate = :galleryRegDate, g.galleryContent = :galleryContent, g.galleryFileName = :galleryFileName, g.galleryFileExtension = :galleryFileExtension WHERE g.galleryIdx = :galleryIdx AND g.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetGalleryTitleAndGalleryRegDateAndGalleryContentAndGalleryFileNameAndGalleryFileExtensionByGalleryIdxAndIdx(@Param("galleryTitle") String galleryTitle, @Param("galleryRegDate") String galleryRegDate, @Param("galleryContent") String galleryContent, @Param("galleryFileName") String galleryFileName, @Param("galleryFileExtension") String galleryFileExtension, @Param("galleryIdx") int galleryIdx, @Param("idx") int idx);

    // 구해낸 게시글 좋아요 개수를 보여주기 위해 컬럼에 작성하기
    @Query("UPDATE Gallery g SET g.galleryLikeNum = :galleryLikeNum WHERE g.galleryIdx = :galleryIdx AND g.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetGalleryLikeNumByGalleryIdxIdxAndIdx(@Param("galleryLikeNum") int galleryLikeNum, @Param("galleryIdx") int galleryIdx, @Param("idx") int idx);
}