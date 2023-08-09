package com.social.cyworld.repository;

import com.social.cyworld.entity.Ilchonpyeong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IlchonpyeongRepository extends JpaRepository<Ilchonpyeong, Object> {
    // 일촌평 전체 목록 조회
    List<Ilchonpyeong> findByIlchonpyeongIdx(int idx);
}
