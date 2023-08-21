package com.social.cyworld.repository;

import com.social.cyworld.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Object> {
    // API 정보 조회
    ApiKey findByIdx(int idx);

    // API 정보 RedirectURI 검증
    ApiKey findByRedirectUri(String redirectUri);

    // API RedirectURI 및 동의 항목 설정
    @Query("UPDATE ApiKey ak SET ak.redirectUri = :redirectUri, ak.gender = :gender, ak.name = :name, ak.birthday = :birthday, ak.phoneNumber = :phoneNumber, ak.email = :email WHERE ak.idx = :idx")
    @Modifying(clearAutomatically = true)
    @Transactional
    int updateSetRedirectUriAndGenderAndNameAndBirthdayAndPhoneNumberAndEmailByIdx(@Param("redirectUri") String redirectUri, @Param("gender") int gender, @Param("name") int name, @Param("birthday") int birthday, @Param("phoneNumber") int phoneNumber, @Param("email") int email, @Param("idx") int idx);
}
