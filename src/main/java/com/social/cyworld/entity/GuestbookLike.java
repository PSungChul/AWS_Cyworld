package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "GuestbookLike")
public class GuestbookLike {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int guestbookLikeIdx;

	@Column(nullable = false)
	private int guestbookLikeRef;

	@Column(nullable = false)
	private int guestbookLikeSessionIdx;
}