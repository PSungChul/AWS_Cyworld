package com.social.cyworld.repository;

import com.social.cyworld.entity.BuyProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyProductRepository extends JpaRepository<BuyProduct, Object> {
    // 구매한 미니미 전체 목록 조회
    List<BuyProduct> findByBuyIdx(int idx);

    // 이미 구매한 미니미인지 조회
    BuyProduct findByBuyIdxAndBuyName(int buyIdx, String buyName);
}
