package com.social.cyworld.dto;

import com.social.cyworld.entity.ApiKey;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApiDTO {
    private int idx;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private int gender;

    private int name;

    private int birthday;

    private int phoneNumber;

    private int email;

    private Integer consent;

    private String code;

    public void rqApiConsent(ApiKey apiKey) {
        this.idx = apiKey.getIdx();
        this.clientId = apiKey.getClientId();
        this.redirectUri = apiKey.getRedirectUri();
        this.gender = apiKey.getGender();
        this.name = apiKey.getName();
        this.birthday = apiKey.getBirthday();
        this.phoneNumber = apiKey.getPhoneNumber();
        this.email = apiKey.getEmail();
    }

    public ApiKey toEntity() {
        return ApiKey.builder()
                .idx(idx)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .gender(gender)
                .name(name)
                .birthday(birthday)
                .phoneNumber(phoneNumber)
                .email(email)
                .build();
    }
}
