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
@Entity(name = "UserProfile")
public class UserProfile {
    @Id
    private String idx;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(length = 5, nullable = false)
    private String gender;

    @Column(length = 15, nullable = false)
    private String name;

    @Column(length = 10, nullable = false)
    private String birthday;

    @Column(length = 30, nullable = false, unique = true)
    private String phoneNumber;
}
