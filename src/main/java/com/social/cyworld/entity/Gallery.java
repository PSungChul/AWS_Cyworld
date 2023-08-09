package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Gallery")
public class Gallery {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int galleryIdx;

	@Column(length = 100, nullable = false)
	private String galleryTitle;

	@Column(length = 30, nullable = false)
	private String galleryRegDate;

	@Column(length = 255, nullable = false)
	private String galleryContent;

	@Column(length = 50, nullable = false)
	private String galleryFileName;

	@Column(length = 10, nullable = false)
	private String galleryFileExtension;

	@Column(length = 38, nullable = false)
	private int galleryLikeNum;
}