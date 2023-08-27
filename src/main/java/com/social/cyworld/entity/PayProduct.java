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
@Entity(name = "PayProduct")
public class PayProduct {
    @Id
    private String orderId;

    @Column(nullable = false)
    private int idx;

    @Column(length = 50, nullable = false)
    private String name;
}
