package com.social.cyworld.repository;

import com.social.cyworld.entity.PayProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayProductRepository extends JpaRepository<PayProduct, Object> {
}
