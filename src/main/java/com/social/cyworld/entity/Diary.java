package com.social.cyworld.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "Diary")
public class Diary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idx;

	@Column(nullable = false)
	private int diaryIdx;

	@Column(length = 30, nullable = false)
	private String diaryRegDate; // 댓글 작성시간

	@Column(length = 255, nullable = false)
	private String diaryContent; // 다이어리 내용
}