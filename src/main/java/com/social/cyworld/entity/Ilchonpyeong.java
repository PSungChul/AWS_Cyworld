package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Ilchonpyeong")
public class Ilchonpyeong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(nullable = false)
    private int ilchonpyeongIdx;

    @Column(length = 50, nullable = false)
    private String ilchonpyeongSessionName;

    @Column(length = 255, nullable = false)
    private String ilchonpyeongContent;
}
