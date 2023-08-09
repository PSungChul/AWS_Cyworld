package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "GalleryLike")
public class GalleryLike {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int galleryLikeIdx;

	@Column(nullable = false)
	private int galleryLikeRef;

	@Column(nullable = false)
	private int galleryLikeSessionIdx;
}