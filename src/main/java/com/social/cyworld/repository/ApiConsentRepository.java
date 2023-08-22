package com.social.cyworld.repository;

import com.social.cyworld.entity.ApiConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiConsentRepository extends JpaRepository<ApiConsent, Object> {
    // API 동의 항목 체크 정보 조회
    ApiConsent findByIdx(int idx);
}
