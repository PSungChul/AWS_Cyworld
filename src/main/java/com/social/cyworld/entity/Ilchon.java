package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Ilchon")
public class Ilchon {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int ilchonIdx;

	@Column(nullable = false)
	private int ilchonSessionIdx;

	@Column(length = 100, nullable = false)
	private String ilchonName;

	@Column(nullable = false)
	private int ilchonUp;
}
