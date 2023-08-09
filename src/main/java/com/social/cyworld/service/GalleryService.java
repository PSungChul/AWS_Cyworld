package com.social.cyworld.service;

import com.social.cyworld.entity.Gallery;
import com.social.cyworld.entity.GalleryComment;
import com.social.cyworld.entity.GalleryLike;
import com.social.cyworld.repository.GalleryCommentRepository;
import com.social.cyworld.repository.GalleryLikeRepository;
import com.social.cyworld.repository.GalleryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class GalleryService {
    @Autowired
    GalleryRepository galleryRepository;

    @Autowired
    GalleryCommentRepository galleryCommentRepository;

    @Autowired
    GalleryLikeRepository galleryLikeRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Gallery
    // 사진첩 전체 목록 조회
    public List<Gallery> findByGalleryIdxOrderByIdxDesc(int idx) {
        List<Gallery> galleryList = galleryRepository.findByGalleryIdxOrderByIdxDesc(idx);
        return galleryList;
    }

    // 새 게시글 추가
    public void insertIntoGallery(Gallery gallery) {
        galleryRepository.save(gallery);
    }

    // 게시글 삭제
    public int deleteByGalleryIdxAndIdx(Gallery gallery) {
        int res = galleryRepository.deleteByGalleryIdxAndIdx(gallery.getGalleryIdx(), gallery.getIdx());
        return res;
    }

    // 수정을 위한 게시글 한 건 조회
    public Gallery findByGalleryIdxAndIdx(Gallery gallery) {
        Gallery updateGallery = galleryRepository.findByGalleryIdxAndIdx(gallery.getGalleryIdx(), gallery.getIdx());
        return updateGallery;
    }

    // 게시글 수정
    public int updateSetGalleryTitleAndGalleryRegDateAndGalleryContentAndGalleryFileNameAndGalleryFileExtensionByGalleryIdxAndIdx(Gallery gallery) {
        int res = galleryRepository.updateSetGalleryTitleAndGalleryRegDateAndGalleryContentAndGalleryFileNameAndGalleryFileExtensionByGalleryIdxAndIdx(gallery.getGalleryTitle(), gallery.getGalleryRegDate(), gallery.getGalleryContent(), gallery.getGalleryFileName(), gallery.getGalleryFileExtension(), gallery.getGalleryIdx(), gallery.getIdx());
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Gallery - Comment
    // 댓글 조회 - 내림차순
    public List<GalleryComment> findByGalleryCommentIdxOrderByIdxDesc(int galleryCommentIdx) {
        List<GalleryComment> galleryCommentList = galleryCommentRepository.findByGalleryCommentIdxOrderByIdxDesc(galleryCommentIdx);
        return galleryCommentList;
    }

    // 새 댓글 추가
    public GalleryComment insertIntoGalleryComment(GalleryComment galleryComment) {
        GalleryComment res = galleryCommentRepository.save(galleryComment);
        return res;
    }

    // 댓글 삭제 - 진짜 삭제가 아닌 삭제 표시로 업데이트
    public int updateSetGalleryCommentDeleteCheckByGalleryCommentIdxAndGalleryIdxCommentAndIdx(GalleryComment galleryComment) {
        int res = galleryCommentRepository.updateSetGalleryCommentDeleteCheckByGalleryCommentIdxAndGalleryIdxCommentAndIdx(galleryComment.getGalleryCommentDeleteCheck(), galleryComment.getGalleryCommentIdx(), galleryComment.getGalleryIdxComment(), galleryComment.getIdx());
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Gallery - Like
    // 좋아요를 이미 눌렀는지 확인하기 위한 작업
    public GalleryLike findByGalleryLikeIdxAndGalleryLikeRefAndGalleryLikeSessionIdx(GalleryLike galleryLike) {
        GalleryLike galleryLikeCheck = galleryLikeRepository.findByGalleryLikeIdxAndGalleryLikeRefAndGalleryLikeSessionIdx(galleryLike.getGalleryLikeIdx(), galleryLike.getGalleryLikeRef(), galleryLike.getGalleryLikeSessionIdx());
        return galleryLikeCheck;
    }

    // 게시글 좋아요 추가
    @Transactional
    public void insertIntoGalleryLike(GalleryLike galleryLike) {
        galleryLikeRepository.save(galleryLike);
    }

    // 게시글 좋아요 취소
    public void deleteByGalleryLikeIdxAndGalleryIdxLikeAndGalleryLikeSessionIdx(GalleryLike galleryLike) {
        galleryLikeRepository.deleteByGalleryLikeIdxAndGalleryIdxLikeAndGalleryLikeSessionIdx(galleryLike.getGalleryLikeIdx(), galleryLike.getGalleryLikeRef(), galleryLike.getGalleryLikeSessionIdx());
    }

    // 게시글 좋아요 개수 구하기
    public int countByGalleryLikeIdxAndGalleryLikeRef(GalleryLike galleryLike) {
        int likeCount = galleryLikeRepository.countByGalleryLikeIdxAndGalleryLikeRef(galleryLike.getGalleryLikeIdx(), galleryLike.getGalleryLikeRef());
        return likeCount;
    }

    // 구해낸 게시글 좋아요 개수를 보여주기 위해 컬럼에 작성하기
    public void updateSetGalleryLikeNumByGalleryIdxIdxAndIdx(Gallery gallery) {
        galleryRepository.updateSetGalleryLikeNumByGalleryIdxIdxAndIdx(gallery.getGalleryLikeNum(), gallery.getGalleryIdx(), gallery.getIdx());
    }
}
