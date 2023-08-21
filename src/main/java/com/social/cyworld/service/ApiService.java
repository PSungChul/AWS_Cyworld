package com.social.cyworld.service;

import com.social.cyworld.entity.ApiKey;
import com.social.cyworld.repository.ApiKeyRepository;
import com.social.cyworld.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiService {
    @Autowired
    ApiKeyRepository apiKeyRepository;
    @Autowired
    SignRepository signRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API 정보 조회
    public ApiKey findByIdx(int idx) {
        ApiKey apiKey = apiKeyRepository.findByIdx(idx);
        return apiKey;
    }

    // API 정보 발급
    public void insertIntoApiKey(ApiKey apiKey) {
        apiKeyRepository.save(apiKey);
    }

    // API 정보 RedirectURI 검증
    public ApiKey findByRedirectUri(String redirectUri) {
        ApiKey apiKey = apiKeyRepository.findByRedirectUri(redirectUri);
        return apiKey;
    }

    // API RedirectURI 및 동의 항목 설정
    public int updateSetRedirectUriAndGenderAndNameAndBirthdayAndPhoneNumberAndEmailByIdx(ApiKey apiKey) {
        int check = apiKeyRepository.updateSetRedirectUriAndGenderAndNameAndBirthdayAndPhoneNumberAndEmailByIdx(apiKey.getRedirectUri(), apiKey.getGender(), apiKey.getName(), apiKey.getBirthday(), apiKey.getPhoneNumber(), apiKey.getEmail(), apiKey.getIdx());
        return check;
    }

    // 유저 정보 API 동의 항목 체크
    public void updateSetConsentByIdx(int idx) {
        signRepository.updateSetConsentByIdx(idx);
    }
}
