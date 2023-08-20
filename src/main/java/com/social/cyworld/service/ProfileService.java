package com.social.cyworld.service;

import com.social.cyworld.entity.BuyMinimi;
import com.social.cyworld.entity.Sign;
import com.social.cyworld.repository.BuyMinimiRepository;
import com.social.cyworld.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    @Autowired
    SignRepository signRepository;

    @Autowired
    BuyMinimiRepository buyMinimiRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile
    // 프로필 미니미 변경
    public void updateSetMinimiByIdx(Sign sign) {
        signRepository.updateSetMinimiByIdx(sign.getMinimi(), sign.getIdx());
    }

    // 프로필 좌측 - 메인 사진 및 메인 소개글 수정
    public void updateSetMainPhotoAndMainTextByIdx(Sign sign) {
        signRepository.updateSetMainPhotoAndMainTextByIdx(sign.getMainPhoto(), sign.getMainText(), sign.getIdx());
    }

    // 프로필 우측 (cyworld 가입자) - 메인 타이틀 및 비밀번호 수정
    public int updateSetInfoAndMainTitleByIdx(Sign sign) {
        int res = signRepository.updateSetInfoAndMainTitleByIdx(sign.getInfo(), sign.getMainTitle(), sign.getIdx());
        return res;
    }

    // 프로필 우측 (소셜 가입자) - 비밀번호 없ㅇ ㅣ메인 타이틀만 수정
    public int updateSetMainTitleByIdx(Sign sign) {
        int res = signRepository.updateSetMainTitleByIdx(sign.getMainTitle(), sign.getIdx());
        return res;
    }

    // 휴대폰 번호 변경
    public int updateSetPhoneNumberByIdx(Sign sign) {
        int res = signRepository.updateSetPhoneNumberByIdx(sign.getPhoneNumber(), sign.getIdx());
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile - BuyMinimi
    // 구매한 미니미 전체 조회
    public List<BuyMinimi> findByBuyIdx(int idx) {
        List<BuyMinimi> buyMinimi = buyMinimiRepository.findByBuyIdx(idx);
        return buyMinimi;
    }

    // 구매한 미니미 추가
    public void insertIntoBuyMinimi(BuyMinimi buyMinimi) {
        buyMinimiRepository.save(buyMinimi);
    }

    // 이미 구매한 미니미인지 조회
    public BuyMinimi findByBuyIdxAndBuyMinimiName(BuyMinimi buyMinimi) {
        BuyMinimi boughtMinimi = buyMinimiRepository.findByBuyIdxAndBuyMinimiName(buyMinimi.getBuyIdx(), buyMinimi.getBuyMinimiName());
        return boughtMinimi;
    }
}
