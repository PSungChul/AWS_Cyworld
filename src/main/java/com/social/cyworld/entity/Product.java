package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(nullable = false)
    private int productType;

    @Column(length = 255, nullable = false)
    private String productIdx;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;
}
