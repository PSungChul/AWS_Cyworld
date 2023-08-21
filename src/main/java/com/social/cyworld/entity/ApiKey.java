package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "ApiKey")
public class ApiKey {
    @Id
    private Integer idx;

    @Column(length = 255, nullable = false, unique = true)
    private String clientId;

    @Column(length = 255, nullable = false, unique = true)
    private String clientSecret;

    @Column(length = 255)
    private String redirectUri;

    @Column
    private int gender;

    @Column
    private int name;

    @Column
    private int birthday;

    @Column
    private int phoneNumber;

    @Column
    private int email;
}
