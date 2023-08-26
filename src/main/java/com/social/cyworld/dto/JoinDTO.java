package com.social.cyworld.dto;

import com.social.cyworld.entity.Sign;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JoinDTO {
    private Integer idx;

    private String email;

    private String emailKey;

    private String info;

    private String gender;

    private String name;

    private String birthday;

    private String phoneNumber;

    private String phoneKey;

    private String platform;

    public Sign toEntity() {
        return Sign.builder()
                .email(email)
                .info(info)
                .gender(gender)
                .name(name)
                .birthday(birthday)
                .phoneNumber(phoneNumber)
                .platform(platform)
                .build();
    }
}
