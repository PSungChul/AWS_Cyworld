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
@Entity(name = "UserLogin")
public class UserLogin {
    @Id
    private String idx;

    @Column(length = 255, nullable = false, unique = true)
    private String userId;

    @Column(length = 255, nullable = false)
    private String info;
}
