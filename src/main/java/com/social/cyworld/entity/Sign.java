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

	@Column(length = 255, nullable = false, unique = true)
	private String mUid;

	@Column(length = 255, nullable = false, unique = true)
	private String pUid;

	@Column(length = 255, nullable = false, unique = true)
	private String lUid;

	@Column(length = 10, nullable = false)
	private String platform;

	@Column(length = 255, nullable = false)
	private String roles;

	@Column
	private int consent;
}