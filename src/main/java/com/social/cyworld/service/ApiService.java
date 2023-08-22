package com.social.cyworld.service;

import com.social.cyworld.entity.ApiConsent;
import com.social.cyworld.entity.ApiKey;
import com.social.cyworld.repository.ApiConsentRepository;
import com.social.cyworld.repository.ApiKeyRepository;
import com.social.cyworld.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiService {
    @Autowired
    SignRepository signRepository;
    @Autowired
    ApiKeyRepository apiKeyRepository;
    @Autowired
    ApiConsentRepository apiConsentRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API 정보 조회
    public ApiKey findByApiKeyIdx(int idx) {
        ApiKey apiKey = apiKeyRepository.findByIdx(idx);
        return apiKey;
    }

    // API Key 발급
    public void insertIntoApiKey(ApiKey apiKey) {
        apiKeyRepository.save(apiKey);
    }

    // API RedirectURI 검증 및 API 정보 조회
    public ApiKey findByRedirectUri(String redirectUri) {
        ApiKey apiKey = apiKeyRepository.findByRedirectUri(redirectUri);
        return apiKey;
    }

    // API RedirectURI 및 동의 항목 설정
    public int updateSetRedirectUriAndGenderAndNameAndBirthdayAndPhoneNumberAndEmailByIdx(ApiKey apiKey) {
        int check = apiKeyRepository.updateSetRedirectUriAndGenderAndNameAndBirthdayAndPhoneNumberAndEmailByIdx(apiKey.getRedirectUri(), apiKey.getGender(), apiKey.getName(), apiKey.getBirthday(), apiKey.getPhoneNumber(), apiKey.getEmail(), apiKey.getIdx());
        return check;
    }

    // API 동의 항목 체크 정보 조회
    public ApiConsent findByApiConsentIdx(int idx) {
        ApiConsent apiConsent = apiConsentRepository.findByIdx(idx);
        return apiConsent;
    }

    // API 동의 항목 체크 저장
    public void insertIntoApiConsent(ApiConsent apiConsent) {
        apiConsentRepository.save(apiConsent);
    }

    // API 로그인 유저 API 동의 항목 체크 완료
    public void updateSetConsentByIdx(int idx) {
        signRepository.updateSetConsentByIdx(idx);
    }
}
