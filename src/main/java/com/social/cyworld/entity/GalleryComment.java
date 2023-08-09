package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "GalleryComment")
public class GalleryComment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int galleryCommentIdx;

	@Column(nullable = false)
	private int galleryIdxComment;

	@Column(nullable = false)
	private int galleryCommentSessionIdx;

	@Column(length = 50, nullable = false)
	private String galleryCommentName;

	@Column(length = 30, nullable = false)
	private String galleryCommentRegDate;

	@Column(length = 255, nullable = false)
	private String galleryCommentContent;

	@Column(nullable = false)
	private int galleryCommentDeleteCheck;
}