package com.social.cyworld.repository;

import com.social.cyworld.entity.BuyMinimi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyMinimiRepository extends JpaRepository<BuyMinimi, Object> {
    // 구매한 미니미 전체 목록 조회
    List<BuyMinimi> findByBuyIdx(int idx);

    // 이미 구매한 미니미인지 조회
    BuyMinimi findByBuyIdxAndBuyMinimiName(int buyIdx, String buyMinimiName);
}
