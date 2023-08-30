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
@Entity(name = "UserMain")
public class UserMain {
    @Id
    private String idx;

    @Column(nullable = false)
    private int dotory;

    @Column(length = 30, nullable = false)
    private String minimi;

    @Column(nullable = false)
    private int ilchon;

    @Column(length = 100)
    private String mainTitle;

    @Column(length = 100)
    private String mainPhoto;

    @Column(length = 255)
    private String mainText;

    @Column(nullable = false)
    private int today;

    @Column(nullable = false)
    private int total;

    @Column(length = 20, nullable = false)
    private String toDate;
}
