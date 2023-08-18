# AWS_Cyworld
AWS를 통해 CyworldProject를 서버에 배포<br>
(보안성으로 인해 서버는 따로 공개 X)

#

## 컨셉
	과거의 싸이월드 재구현을 통한 추억 여행을 목적으로 미니홈피를 제작해 홈, 프로필, 다이어리, 사진첩, 방명록을 사용할 수 있게 설계하였습니다.

#

## 구성원 및 기간
	프론트엔드 1명, 백엔드 4명 총 5명이서 팀을 꾸려 2주간(2022.11.22 ~ 2022.12.6) CyworldProject를 진행하였습니다.
	이후 혼자서 유지 보수 및 추가적인 기능 작업들과 더불어 AWS를 통해 CyworldProject를 서버에 배포하는 작업을 진행 중입니다.

#

## 내가 구현한 주요 기능들
### 로그인 & 회원가입
	naver Login API
	kakao Login API
	이메일 인증 SMTP
	주소 API
	아이디 찾기
	임시 비밀번호 발급
### 메인
	투데이 (조회수)
	파도타기 (일촌)
	일촌 신청
	일촌평 작성
	유저 검색
### 프로필
	메인 사진 및 메인 소개글 작성 및 수정
	메인 타이틀 작성 및 수정
	비밀번호 수정
	미니미 변경
### 사진첩 & 방명록
	좋아요
### 부가 역할들
	데이터베이스 전체 생성 및 관리
	프로젝트 모든 파일 병합
### 프로젝트 이후 추가한 기능들
	휴대폰 문자 인증
	인증번호 암호화
	비밀번호 암호화
 	Token 및 Redis를 추가하여 로그인 보안 강화
  	로그인 토큰 관리에 헤더 대신 쿠키로 변경

#

## 개발환경
	Mac
	Eclipse --> Intellij
	Spring --> Spring Boot
	Maven --> Gradle
	MyBatis --> JPA
	Oracle --> MySQL
	먼저 왼쪽 환경으로 제작을 하였고, 이후 AWS를 통해 CyworldProject를 서버에 배포하기 위해 새로운 환경으로 재구성 하였습니다.

#

## Database
### DATABASE - Cyworld
	# 데이터베이스 생성
	CREATE DATABASE Cyworld DEFAULT CHARACTER SET UTF8MB4;
	# 생성한 데이터베이스 사용
	USE Cyworld;
### TABLE - Sign, Views, Ilchon, Ilchonpyeong, BuyMinimi, Diary, Gallery, GalleryLike, GalleryComment, Guestbook, GuestbookLike
	# 테이블 생성
	
	# 유저 테이블
	CREATE TABLE Sign (
		idx  INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		userId VARCHAR(15) NOT NULL UNIQUE, # ID
		info VARCHAR(255) NOT NULL, # PW
		name VARCHAR(15) NOT NULL, # 이름
		gender VARCHAR(5) NOT NULL, # 성별
		email VARCHAR(50) NOT NULL UNIQUE, # Email
		phoneNumber VARCHAR(30) NOT NULL UNIQUE, # 휴대전화
		address VARCHAR(255) NOT NULL, # 주소
		addressDetail VARCHAR(255) NOT NULL, # 상세주소
		platform VARCHAR(10) NOT NULL, # 플랫폼
		minimi VARCHAR(30) NOT NULL, # 미니미
		dotory INT NOT NULL, # 도토리 개수
		ilchon INT NOT NULL, # 일촌 수
		mainTitle VARCHAR(100), # 메인 타이틀
		mainPhoto VARCHAR(100), # 메인 사진
		mainText VARCHAR(255), # 메인 소개글
		today INT NOT NULL, # 일일 조회수
		total INT NOT NULL, # 총합 조회수
		toDate VARCHAR(20) NOT NULL # 접속 일자
	);
	
	# 조회수 테이블
	CREATE TABLE Views (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		viewsIdx INT NOT NULL, # 미니홈피 유저 IDX
		viewsSessionIdx INT NOT NULL, # 로그인 유저 IDX
		todayDate VARCHAR(20) NOT NULL # 접속 일자
	);
	
	# 일촌 테이블
	CREATE TABLE Ilchon (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		ilchonIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_IlchonIdx FOREIGN KEY(ilchonIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		ilchonSessionIdx INT NOT NULL, # 로그인 유저 IDX
		CONSTRAINT fk_IlchonSessionIdx FOREIGN KEY(ilchonSessionIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		ilchonName VARCHAR(100) NOT NULL, # 일촌 이름
		ilchonUp INT NOT NULL # 일촌 수
	);
	
	
	# 일촌평 테이블
	CREATE TABLE Ilchonpyeong (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		ilchonpyeongIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_IlchonpyeongIdx FOREIGN KEY(ilchonpyeongIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		ilchonpyeongSessionName VARCHAR(50), # 로그인 유저 이름
		ilchonpyeongContent VARCHAR(255) NOT NULL # 일촌평 내용
	);
	
	# 미니미 구매 테이블
	CREATE TABLE BuyMinimi (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		buyIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_BuyIdx FOREIGN KEY(buyIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		buyMinimiName VARCHAR(50) NOT NULL UNIQUE # 구매한 미니미 이름
	);
	
	# 다이어리 테이블
	CREATE TABLE Diary (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		diaryIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_DiaryIdx FOREIGN KEY(diaryIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		diaryRegDate VARCHAR(30) NOT NULL, # 다이어리 글 작성 일자
		diaryContent VARCHAR(255) NOT NULL # 다이어리 글 내용
	);
	
	# 갤러리 테이블
	CREATE TABLE Gallery (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		galleryIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_GalleryIdx FOREIGN KEY(galleryIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		galleryTitle VARCHAR(100) NOT NULL, # 게시글 제목
		galleryRegDate VARCHAR(30) NOT NULL, # 게시글 작성 일자
		galleryContent VARCHAR(255) NOT NULL, # 게시글 내용
		galleryFileName VARCHAR(50) NOT NULL, # 게시글 파일 이름
		galleryFileExtension VARCHAR(10) NOT NULL, # 게시글 파일 확장자
		galleryLikeNum INT NOT NULL # 게시글 좋아요 수
	);
	
	# 갤러리 좋아요 테이블
	CREATE TABLE GalleryLike (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		galleryLikeIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_GalleryLikeIdx FOREIGN KEY(galleryLikeIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		galleryLikeRef INT NOT NULL, # 게시글 번호
		CONSTRAINT fk_GalleryLikeRef FOREIGN KEY(galleryLikeRef) REFERENCES Gallery(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		galleryLikeSessionIdx INT NOT NULL, # 로그인 유저 IDX
		CONSTRAINT fk_GalleryLikeSessionIdx FOREIGN KEY(galleryLikeSessionIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE # 포린키 연결
	);
	
	# 갤러리 댓글 테이블
	CREATE TABLE GalleryComment (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		galleryCommentIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_GalleryCommentIdx FOREIGN KEY(galleryCommentIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		galleryIdxComment INT NOT NULL, # 게시글 번호
		CONSTRAINT fk_GalleryIdxComment FOREIGN KEY(galleryIdxComment) REFERENCES Gallery(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		galleryCommentSessionIdx INT NOT NULL, # 로그인 유저 IDX
		CONSTRAINT fk_GalleryCommentSessionIdx FOREIGN KEY(galleryCommentSessionIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		galleryCommentName VARCHAR(50) NOT NULL, # 게시글 댓글 작성자 이름
		galleryCommentRegDate VARCHAR(30) NOT NULL, # 게시글 댓글 작성 일자
		galleryCommentContent VARCHAR(255) NOT NULL, # 게시글 댓글 내용
		galleryCommentDeleteCheck INT NOT NULL # 게시글 댓글 삭제 여부
	);
	
	# 방명록 테이블
	CREATE TABLE Guestbook (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		guestbookIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_GuestbookIdx FOREIGN KEY(guestbookIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		guestbookSessionIdx INT NOT NULL, # 로그인 유저 IDX
		CONSTRAINT fk_GuestbookSessionIdx FOREIGN KEY(guestbookSessionIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		guestbookName VARCHAR(50) NOT NULL, # 방명록 작성자 이름
		guestbookMinimi VARCHAR(50) NOT NULL, # 방명록 작성자 미니미
		guestbookRegDate VARCHAR(30) NOT NULL, # 방명록 작성 일자
		guestbookContent VARCHAR(255) NOT NULL, # 방명록 내용
		guestbookLikeNum INT NOT NULL, # 방명록 좋아요 수
		guestbookSecretCheck INT NOT NULL # 방명록 비밀 여부
	);
	
	# 방명록 좋아요 테이블
	CREATE TABLE GuestbookLike (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		guestbookLikeIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_GuestbookLikeIdx FOREIGN KEY(guestbookLikeIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		guestbookLikeRef INT NOT NULL, # 방명록 번호
		CONSTRAINT fk_GuestbookLikeRef FOREIGN KEY(guestbookLikeRef) REFERENCES Guestbook(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		guestbookLikeSessionIdx INT NOT NULL, # 로그인 유저 IDX
		CONSTRAINT fk_GuestbookLikeSessionIdx FOREIGN KEY(guestbookLikeSessionIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE # 포린키 연결
	);

#
