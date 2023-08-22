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
@Entity(name = "ApiConsent")
public class ApiConsent {
    @Id
    private Integer idx;

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
