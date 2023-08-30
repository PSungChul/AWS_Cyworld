package com.social.cyworld.service;

import com.social.cyworld.entity.BuyProduct;
import com.social.cyworld.entity.UserLogin;
import com.social.cyworld.entity.UserMain;
import com.social.cyworld.entity.UserProfile;
import com.social.cyworld.repository.BuyProductRepository;
import com.social.cyworld.repository.UserLoginRepository;
import com.social.cyworld.repository.UserMainRepository;
import com.social.cyworld.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    UserMainRepository userMainRepository;
    @Autowired
    BuyProductRepository buyProductRepository;
    @Autowired
    UserLoginRepository userLoginRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile
    // 프로필 미니미 변경
    public void updateSetMinimiByIdx(UserMain userMain, int idx) {
        userMainRepository.updateSetMinimiByIdx(userMain.getMinimi(), idx);
    }

    // 프로필 좌측 - 메인 사진 및 메인 소개글 수정
    public void updateSetMainPhotoAndMainTextByIdx(UserMain userMain, int idx) {
        userMainRepository.updateSetMainPhotoAndMainTextByIdx(userMain.getMainPhoto(), userMain.getMainText(), idx);
    }

    // 프로필 우측 - 비밀번호 수정
    public int updateSetInfoByIdx(UserLogin userLogin, int idx) {
        int res = userLoginRepository.updateSetInfoByIdx(userLogin.getInfo(), idx);
        return res;
    }

    // 프로필 우측 - 비밀번호 없이 메인 타이틀만 수정
    public int updateSetMainTitleByIdx(UserMain userMain, int idx) {
        int res = userMainRepository.updateSetMainTitleByIdx(userMain.getMainTitle(), idx);
        return res;
    }

    // 휴대폰 번호 변경
    public int updateSetPhoneNumberByIdx(UserProfile userProfile, int idx) {
        int res = userProfileRepository.updateSetPhoneNumberByIdx(userProfile.getPhoneNumber(), idx);
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Profile - BuyMinimi
    // 구매한 미니미 전체 조회
    public List<BuyProduct> findByBuyIdx(int idx) {
        List<BuyProduct> buyProduct = buyProductRepository.findByBuyIdx(idx);
        return buyProduct;
    }

    // 구매한 미니미 추가
    public void insertIntoBuyProduct(BuyProduct buyProduct) {
        buyProductRepository.save(buyProduct);
    }

    // 이미 구매한 미니미인지 조회
    public BuyProduct findByBuyIdxAndBuyName(BuyProduct buyProduct) {
        BuyProduct boughtMinimi = buyProductRepository.findByBuyIdxAndBuyName(buyProduct.getBuyIdx(), buyProduct.getBuyName());
        return boughtMinimi;
    }
}
