package com.social.cyworld.service;

import com.social.cyworld.entity.Guestbook;
import com.social.cyworld.entity.GuestbookLike;
import com.social.cyworld.repository.GuestbookLikeRepository;
import com.social.cyworld.repository.GuestbookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class GuestbookService {
    @Autowired
    GuestbookRepository guestbookRepository;
    @Autowired
    GuestbookLikeRepository guestbookLikeRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Guestbook
    // 방명록 전체 조회 - 내림차순
    public List<Guestbook> findByGuestbookIdxOrderByIdxDesc(int idx) {
        List<Guestbook> guestbookList = guestbookRepository.findByGuestbookIdxOrderByIdxDesc(idx);
        return guestbookList;
    }

    // 새 방명록 추가
    public void insertIntoGuestbook(Guestbook guestbook) {
        guestbookRepository.save(guestbook);
    }

    // 방명록 삭제
    public int deleteByGuestbookIdxAndIdx(Guestbook guestbook) {
        int res = guestbookRepository.deleteByGuestbookIdxAndIdx(guestbook.getGuestbookIdx(), guestbook.getIdx());
        return res;
    }

    // 수정을 위한 방명록 한 건 조회
    public Guestbook findByGuestbookIdxAndIdx(Guestbook guestbook) {
        Guestbook updateGuestbook = guestbookRepository.findByGuestbookIdxAndIdx(guestbook.getGuestbookIdx(), guestbook.getIdx());
        return updateGuestbook;
    }

    // 방명록 수정
    public int updateSetGuestbookNameAndGuestbookMinimiAndGuestbookRegDateAndGuestbookContentAndGuestbookSecretCheckByGuestbookIdxAndGuestbookSessionIdxAndIdx(Guestbook guestbook) {
        int res = guestbookRepository.updateSetGuestbookNameAndGuestbookMinimiAndGuestbookRegDateAndGuestbookContentAndGuestbookSecretCheckByGuestbookIdxAndGuestbookSessionIdxAndIdx(guestbook.getGuestbookName(), guestbook.getGuestbookMinimi(), guestbook.getGuestbookRegDate(), guestbook.getGuestbookContent(), guestbook.getGuestbookSecretCheck(), guestbook.getGuestbookIdx(), guestbook.getGuestbookSessionIdx(), guestbook.getIdx());
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Guestbook - Like
    // 좋아요를 이미 눌렀는지 확인하기 위한 조회
    public GuestbookLike findByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(GuestbookLike guestbookLike) {
        GuestbookLike guestbookLikeCheck = guestbookLikeRepository.findByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(guestbookLike.getGuestbookLikeIdx(),guestbookLike.getGuestbookLikeRef(), guestbookLike.getGuestbookLikeSessionIdx());
        return guestbookLikeCheck;
    }

    // 방명록 좋아요 추가
    @Transactional
    public void insertIntoGuestbookLike(GuestbookLike guestbookLike) {
        guestbookLikeRepository.save(guestbookLike);
    }

    // 방명록 좋아요 취소
    public void deleteByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(GuestbookLike guestbookLike) {
        guestbookLikeRepository.deleteByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(guestbookLike.getGuestbookLikeIdx(),guestbookLike.getGuestbookLikeRef(), guestbookLike.getGuestbookLikeSessionIdx());
    }

    // 방명록 좋아요 개수 구하기
    public int countByGuestbookLikeIdxAndGuestbookLikeRef(GuestbookLike guestbookLike) {
        int likeCount = guestbookLikeRepository.countByGuestbookLikeIdxAndGuestbookLikeRef(guestbookLike.getGuestbookLikeIdx(), guestbookLike.getGuestbookLikeRef());
        return likeCount;
    }

    // 구해낸 방명록 좋아요 개수를 보여주기 위해 컬럼에 작성하기
    public void updateSetGuestbookLikeNumByGuestbookIdxAndIdx(Guestbook guestbook) {
        guestbookRepository.updateSetGuestbookLikeNumByGuestbookIdxAndIdx(guestbook.getGuestbookLikeNum(), guestbook.getGuestbookIdx(), guestbook.getIdx());
    }
}
