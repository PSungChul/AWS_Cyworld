package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Sign")
public class Sign {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(length = 50, nullable = false, unique = true)
	private String email;

	@Column(length = 255, nullable = false)
	private String info;

	@Column(length = 5, nullable = false)
	private String gender;

	@Column(length = 15, nullable = false)
	private String name;

	@Column(length = 10, nullable = false)
	private String birthday;

	@Column(length = 30, nullable = false, unique = true)
	private String phoneNumber;

	@Column(length = 10, nullable = false)
	private String platform;

	@Column(length = 255, nullable = false)
	private String roles;

	@Column(length = 30, nullable = false)
	private String minimi;

	@Column(nullable = false)
	private int dotory;

	@Column(nullable = false)
	private int ilchon;

	// 메인화면에 작성되는 요소들

	@Column(length = 100)
	private String mainTitle;

	@Column(length = 100)
	private String mainPhoto;

	@Column(length = 255)
	private String mainText;

	// 조회수 기록을 위한 요소들

	@Column(nullable = false)
	private int today;

	@Column(nullable = false)
	private int total;

	@Column(length = 20, nullable = false)
	private String toDate;

	@Column
	private int consent;
}