package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Views")
public class Views {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int viewsIdx;

	@Column(nullable = false)
	private int viewsSessionIdx;

	@Column(length = 20, nullable = false)
	private String todayDate;
}