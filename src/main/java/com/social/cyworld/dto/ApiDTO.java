package com.social.cyworld.dto;

import com.social.cyworld.entity.ApiConsent;
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

    // API DTO를 ApiConsent Entity로 변환 (빌더 방식)
    public ApiConsent toApiConsent() {
        return ApiConsent.builder()
                .idx(idx)
                .gender(gender)
                .name(name)
                .birthday(birthday)
                .phoneNumber(phoneNumber)
                .email(email)
                .build();
    }
}
