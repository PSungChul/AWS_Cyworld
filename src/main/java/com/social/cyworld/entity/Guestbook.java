package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Guestbook")
public class Guestbook {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int guestbookIdx;

	@Column(nullable = false)
	private int guestbookSessionIdx;

	@Column(length = 50, nullable = false)
	private String guestbookName;

	@Column(length = 50, nullable = false)
	private String guestbookMinimi;

	@Column(length = 30, nullable = false)
	private String guestbookRegDate;

	@Column(length = 255, nullable = false)
	private String guestbookContent;

	@Column(nullable = false)
	private int guestbookLikeNum;

	@Column(nullable = false)
	private int guestbookSecretCheck;
}