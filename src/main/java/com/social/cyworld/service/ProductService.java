package com.social.cyworld.service;

import com.social.cyworld.entity.PayProduct;
import com.social.cyworld.entity.Product;
import com.social.cyworld.repository.PayProductRepository;
import com.social.cyworld.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    PayProductRepository payProductRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 상품 추가
    public void insertIntoProduct(Product product) {
        productRepository.save(product);
    }

    // 상품 타입에 해당하는 상품 정보를 모두 조회
    public List<Product> findByProductType(int productType) {
        List<Product> productList = productRepository.findByProductType(productType);
        return productList;
    }

    // 상품 번호에 해당하는 상품 정보를 조회
    public Product findByProductIdx(String productIdx) {
        Product product = productRepository.findByProductIdx(productIdx);
        return product;
    }

    // 상품 결제 내역 저장
    public void insertIntoPayProduct(PayProduct payProduct) {
        payProductRepository.save(payProduct);
    }
}
