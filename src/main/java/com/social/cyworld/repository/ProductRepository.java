package com.social.cyworld.repository;

import com.social.cyworld.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Object> {
    // 상품 타입에 해당하는 상품 정보를 모두 조회
    List<Product> findByProductType(int productType);
    // 상품 번호에 해당하는 상품 정보를 조회
    Product findByProductIdx(String productIdx);
}
