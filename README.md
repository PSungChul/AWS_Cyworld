# AWS_Cyworld
AWS를 통해 CyworldProject를 서버에 배포<br>
(보안성으로 인해 서버는 따로 공개 X)

#

## 📌 컨셉
	과거의 싸이월드 재구현을 통한 추억 여행을 목적으로 미니홈피를 제작해 홈, 프로필, 다이어리, 사진첩, 방명록을 사용할 수 있게 설계하였습니다.

#

## 📌 구성원 및 기간
	프론트엔드 1명, 백엔드 4명 총 5명이서 팀을 꾸려 2주간(2022.11.22 ~ 2022.12.6) CyworldProject를 진행하였습니다.
	이후 혼자서 유지 보수 및 추가적인 기능 작업들과 더불어 AWS를 통해 CyworldProject를 서버에 배포하는 작업을 진행 중입니다.

#

## 📌 내가 구현한 주요 기능들
### ✔ 로그인 & 회원가입
	naver Login API
	kakao Login API
	이메일 인증 SMTP
	주소 API
	아이디 찾기
	임시 비밀번호 발급
### ✔ 메인
	투데이 (조회수)
	파도타기 (일촌)
	일촌 신청
	일촌평 작성
	유저 검색
### ✔ 프로필
	메인 사진 및 메인 소개글 작성 및 수정
	메인 타이틀 작성 및 수정
	비밀번호 수정
	미니미 변경
### ✔ 사진첩 & 방명록
	좋아요
### ✔ 부가 역할들
	데이터베이스 전체 생성 및 관리
	프로젝트 모든 파일 병합
### ✔ 프로젝트 이후 추가한 기능 및 History
	휴대폰 문자 인증 추가
	인증번호 암호화 추가
	비밀번호 암호화 추가
 	Token 및 Redis를 추가하여 로그인 보안 강화
  	로그인 토큰 관리에 헤더 대신 쿠키로 변경
	유저 정보에 생년월일 컬럼 추가
	Cyworld Login API 구현
	ApiKey 테이블 추가, 유저 정보에 API 동의 항목 체크 컬럼 추가
	API 동의 항목을 필수 동의 항목과 선택 동의 항목으로 분리
	ApiConsent 테이블 추가
	URI 형식 변경 (.do 제거)
	팝업 및 각 컨트롤러의 메인 페이지들의 URI 형식을 쿼리 파라미터 형식에서 경로 매개변수 형식으로 변경
	NICEPAY 결제 API 연동
	상품 추가 로직 및 페이지 추가
	상품 정보 테이블 추가
	회원가입 휴대폰 문자 인증 --> 이름 + 생년월일 + 휴대폰 번호에 해당하는 본인인증으로 변경
	본인인증은 IamPort API 사용
	아이디 --> 이메일로 변경 - 아이디 컬럼 제거
	주소 제거 - 주소 컬럼, 상세 주소 컬럼 제거
	이메일 인증 및 본인인증에 쿠키를 추가하여 Controller에서 2차 유효성 검사 진행
	네이버 로그인 API에서 이메일을 가져오는 방식이 변경됨에 따라 네이버 회원가입 로직 변경
	Role 권한 추가 - 권한 컬럼 추가
	관리자 검증 로직 추가
	미니미 구매 로직 변경
	미니미를 상품으로 등록
	상품 번호 암호화 방식 변경
	싱품 주문 번호 생성 방식 변경
	상품 결제 내역 저장
	결제와 구매를 분류
	결제 - 카드 등을 이용한 현실 재화 사용
	구매 - 도토리 등을 이용한 사이트 내부 재화 사용
	미니미 구매 테이블 --> 상품 구매 테이블로 변경
	상품 결제 테이블 추가
	Sign 테이블 --> Sign, UserLogin, UserProfile, UserMain 테이블 정규화
	Sign 테이블의 컬럼들을 각각 사용처에 맞게 분리한 후 각 테이블의 고유 키로 접근할 수 있도록 변경하여 유저 정보 보안 강화
	유저 로그인 정보를 담당하는 UserLogin 테이블, 유저 프로필 정보를 담당하는 UserProfile 테이블, 유저 메인 정보를 담당하는 UserMain 테이블을 각각 추가
	CSS Media Query와 JS Resize Event를 사용하여 메인 페이지에 반응형 웹 적용
	WebSocket과 STOMP를 사용하여 1:1 실시간 채팅 기능 구현
	채팅 및 녹음 메시지 전송
	메시지 읽음 / 안 읽음 구분
	채팅 정보 저장용 MongoDB 추가
	CSS Media Query와 JS Resize Event를 사용하여 채팅 페이지에 반응형 웹 적용
	채팅방 정보에 안 읽은 메시지 수 표시
	Users 컬렉션의 chatRooms에 unreadStatus 추가
	AWS를 통해 HTTPS 추가
	WebSocket을 사용하여 WebRTC와 Signaling Server를 통해 1:1 실시간 영상 통화 기능 구현
	안 읽은 메시지 수에 따라 실시간 채팅방 상태 갱신 기능 구현
	CSS Media Query와 JS Resize Event를 사용하여 로그인 페이지에 반응형 웹 적용

#

## 📌 개발환경
	Mac
	Eclipse --> Intellij
	Spring --> Spring Boot
	Maven --> Gradle
	MyBatis --> JPA
	Oracle --> MySQL
	먼저 왼쪽 환경으로 제작을 하였고, 이후 AWS를 통해 CyworldProject를 서버에 배포하기 위해 새로운 환경으로 재구성 하였습니다.
	Redis - 로그인 토큰 저장용 데이터 스토어
	MongoDB - 채팅 정보 저장용 NoSQL 데이터베이스

#

## 📌 데이터베이스
## MySQL - SQL
### ✔ DATABASE - Cyworld
	# 데이터베이스 생성
	CREATE DATABASE Cyworld DEFAULT CHARACTER SET UTF8MB4;
	# 생성한 데이터베이스 사용
	USE Cyworld;
### ✔ TABLE - Sign, UserLogin, UserProfile, UserMain, Views, Ilchon, Ilchonpyeong, BuyProduct, Diary, Gallery, GalleryLike, GalleryComment, Guestbook, GuestbookLike, ApiKey, ApiConsent, Product, PayProduct
	# 테이블 생성
	
	# 유저 키 테이블
	CREATE TABLE Sign (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		mUid VARCHAR(255) NOT NULL UNIQUE, # 유저 메인 정보 테이블 키
		pUid VARCHAR(255) NOT NULL UNIQUE, # 유저 프로필 정보 테이블 키
		lUid VARCHAR(255) NOT NULL UNIQUE, # 유저 로그인 정보 테이블 키
		platform VARCHAR(10) NOT NULL, # 플랫폼
		roles VARCHAR(255) NOT NULL, # 권한
		consent INT # API 동의 항목 체크
	);
	
	# 유저 로그인 정보 테이블
	CREATE TABLE UserLogin (
		idx VARCHAR(255) PRIMARY KEY, # IDX - 테이블 키
		CONSTRAINT fk_UserLoginIdx FOREIGN KEY(idx) REFERENCES Sign(lUid) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		userId VARCHAR(255) NOT NULL UNIQUE, # ID - 암호화
		info VARCHAR(255) NOT NULL # PW - 암호화
	);
	
	# 유저 프로필 정보 테이블
	CREATE TABLE UserProfile (
		idx VARCHAR(255) PRIMARY KEY, # IDX - 테이블 키
		CONSTRAINT fk_UserProfileIdx FOREIGN KEY(idx) REFERENCES Sign(pUid) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		email VARCHAR(50) NOT NULL UNIQUE, # Email
		gender VARCHAR(5) NOT NULL, # 성별
		name VARCHAR(15) NOT NULL, # 이름
		birthday VARCHAR(10) NOT NULL, # 생년월일
		phoneNumber VARCHAR(30) NOT NULL UNIQUE # 휴대전화
	);
	
	# 유저 메인 정보 테이블
	CREATE TABLE UserMain (
		idx VARCHAR(255) PRIMARY KEY, # IDX - 테이블 키
		CONSTRAINT fk_UserMainIdx FOREIGN KEY(idx) REFERENCES Sign(mUid) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		dotory INT NOT NULL, # 도토리 개수
		minimi VARCHAR(30) NOT NULL, # 미니미
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
		ilchonUp INT NOT NULL # 일촌 상태
	);
	
	
	# 일촌평 테이블
	CREATE TABLE Ilchonpyeong (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		ilchonpyeongIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_IlchonpyeongIdx FOREIGN KEY(ilchonpyeongIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		ilchonpyeongSessionName VARCHAR(50), # 로그인 유저 이름
		ilchonpyeongContent VARCHAR(255) NOT NULL # 일촌평 내용
	);
	
	# 상품 구매 테이블
	CREATE TABLE BuyProduct (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		buyIdx INT NOT NULL, # 미니홈피 유저 IDX
		CONSTRAINT fk_BuyIdx FOREIGN KEY(buyIdx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		buyName VARCHAR(50) NOT NULL UNIQUE # 구매한 상품 이름
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
	
	# API 정보 테이블
	CREATE TABLE ApiKey (
		idx INT PRIMARY KEY, # 로그인 유저 IDX
		CONSTRAINT fk_ApiKeyIdx FOREIGN KEY(idx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		clientId VARCHAR(255) NOT NULL UNIQUE, # API Client ID Key
		clientSecret VARCHAR(255) NOT NULL UNIQUE, # API Client Secret Key
		redirectUri VARCHAR(255), # API Redirect URI
		gender INT, # 성별 동의 항목
		name INT, # 이름 동의 항목
		birthday INT, # 생년월일 동의 항목
		phoneNumber INT, # 휴대폰 번호 동의 항목
		email INT # 이메일 동의 항목
	);
	
	# API 동의 항목 체크 테이블
	CREATE TABLE ApiConsent (
		idx INT PRIMARY KEY, # 로그인 유저 IDX
		CONSTRAINT fk_ApiConsentIdx FOREIGN KEY(idx) REFERENCES Sign(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 포린키 연결
		gender INT, # 성별 동의 항목
		name INT, # 이름 동의 항목
		birthday INT, # 생년월일 동의 항목
		phoneNumber INT, # 휴대폰 번호 동의 항목
		email INT # 이메일 동의 항목
	);
	
	# 상품 정보 테이블
	CREATE TABLE Product (
		idx INT PRIMARY KEY AUTO_INCREMENT, # IDX - 기본키, 시퀀스
		productType INT NOT NULL, # 상품 타입
		productIdx VARCHAR(255) NOT NULL, # 상품 번호
		name VARCHAR(50) NOT NULL, # 상품 이름
		price INT NOT NULL # 상품 가격
	);
	
	# 상품 결제 테이블
	CREATE TABLE PayProduct (
		orderId VARCHAR(255) PRIMARY KEY, # 상품 주문 번호
		idx INT NOT NULL, # 로그인 유저 IDX
		name VARCHAR(50) NOT NULL # 상품 이름
	);

#

## MongoDB - NoSQL
### ✔ DATABASE - Cyworld
	# 데이터베이스 생성 및 사용
	USE Cyworld
	# 데이터베이스 관리자 생성
	db.createUser( { user: "아이디", pwd: "비밀번호", roles: [ { role: "dbOwner", db: "Cyworld" } ] } )
	# 데이터베이스 조작자 생성
	db.createUser( { user: "아이디", pwd: "비밀번호", roles: [ { role: "readWrite", db: "Cyworld" } ] } )

### ✔ COLLECTION - Users, ChatRooms
	# 컬렉션 생성
	db.createCollection( "Users", { capped: true, size: 10000 } )
	db.createCollection( "ChatRooms", { capped: true, size: 10000 } )

	# 유저 정보 컬렉션
	"Users": [
		{
			"_id": "유저 idx",
			"chatRooms": [
				{"_id": "채팅방 아이디1", "idx": "상대 유저 idx", "email": "상대 유저 이메일", "name": "상대 유저 이름", "mainPhoto": "상대 유저 메인 사진", "unreadStatus": "안 읽은 메시지 수"},
				{"_id": "채팅방 아이디2", "idx": "상대 유저 idx", "email": "상대 유저 이메일", "name": "상대 유저 이름", "mainPhoto": "상대 유저 메인 사진", "unreadStatus": "안 읽은 메시지 수"}
			]
		},
		{
			"_id": "유저 idx",
			"chatRooms": [
				{"_id": "채팅방 아이디1", "idx": "상대 유저 idx", "email": "상대 유저 이메일", "name": "상대 유저 이름", "mainPhoto": "상대 유저 메인 사진", "unreadStatus": "안 읽은 메시지 수"},
				{"_id": "채팅방 아이디3", "idx": "상대 유저 idx", "email": "상대 유저 이메일", "name": "상대 유저 이름", "mainPhoto": "상대 유저 메인 사진", "unreadStatus": "안 읽은 메시지 수"}
			]
		}
	]

	# 채팅방 정보 컬렉션
	"ChatRooms": [
		{
			"_id": "채팅방 아이디1",
			"messages": [
				{"type": "메시지 타입", "idx": "전송 유저 idx", "sender": "전송 유저 이름", "mainPhoto": "전송 유저 메인 사진", "content": "메시지 내용", "status": "메시지 상태"},
				{"type": "메시지 타입", "idx": "전송 유저 idx", "sender": "전송 유저 이름", "mainPhoto": "전송 유저 메인 사진", "content": "메시지 내용", "status": "메시지 상태"}
			]
		},
		{
			"_id": "채팅방 아이디2",
			"messages": [
				{"type": "메시지 타입", "idx": "전송 유저 idx", "sender": "전송 유저 이름", "mainPhoto": "전송 유저 메인 사진", "content": "메시지 내용", "status": "메시지 상태"},
				{"type": "메시지 타입", "idx": "전송 유저 idx", "sender": "전송 유저 이름", "mainPhoto": "전송 유저 메인 사진", "content": "메시지 내용", "status": "메시지 상태"}
			]
		},
		{
			"_id": "채팅방 아이디3",
			"messages": [
				{"type": "메시지 타입", "idx": "전송 유저 idx", "sender": "전송 유저 이름", "mainPhoto": "전송 유저 메인 사진", "content": "메시지 내용", "status": "메시지 상태"},
				{"type": "메시지 타입", "idx": "전송 유저 idx", "sender": "전송 유저 이름", "mainPhoto": "전송 유저 메인 사진", "content": "메시지 내용", "status": "메시지 상태"}
			]
		}
	]

#

## 📌 Cyworld Login API 가이드
### ✔ API Login Code 발급
	GET http://localhost:9999/api/cyworld - API 검증 --> API 로그인 --> API 동의 항목 --> API Login Code 발급
	"?clientId=" + Client ID Key + "&redirectUri=" + Redirect URI
	정상
	Redirect URI + "?code=" + API Login Code
	에러 - API 에러 페이지
	http://localhost:9999/api/error + "?code=" + Error Message
### ✔ API Access Token 발급
	POST http://localhost:9999/api/token - API 검증 --> API Login Code 검증 --> API Login Access Token 발급
	Body "clientId": Client ID Key,
	     "clientSecret": Client Secret Key,
	     "redirectUri": Redirect URI,
	     "code": API Login Code
	정상
	{"accessToken": API Access Token}
	에러
	{"accessToken": Error Code,
	 "message": Error Message}
### ✔ API 유저 정보 조회
	POST http://localhost:9999/api/user - API Login Access Token 검증 --> API 유저 정보 조회
	Header "Authorization": "Bearer " + API Access Token
	정상 - 동의 항목 동의
	{"birthday": yyyy-MM-dd,
	 "phoneNumber": 01012345678,
	 "gender": male/female,
	 "name": 테스터,
	 "email": cyworld@cyworld.com}
	정상 - 동의 항목 미동의
	{"birthday": null,
	 "phoneNumber": null,
	 "gender": null,
	 "name": null,
	 "email": null}
	에러 - Invalid API Access Token
	{"user": Error Code}
	에러 - Invalid User
	{"user": null}

#
