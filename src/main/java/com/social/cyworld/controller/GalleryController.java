package com.social.cyworld.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.social.cyworld.dto.GalleryDTO;
import com.social.cyworld.entity.Gallery;
import com.social.cyworld.entity.GalleryComment;
import com.social.cyworld.entity.GalleryLike;
import com.social.cyworld.entity.Sign;
import com.social.cyworld.service.GalleryService;
import com.social.cyworld.service.SignService;
import com.social.cyworld.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/gallery")
@Controller
public class GalleryController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	HttpServletResponse response;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	SignService signService;
	@Autowired
	GalleryService galleryService;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 사진첩 조회
	@RequestMapping("/{idx}") // 경로 매개변수
	public String gallery(@PathVariable int idx, Model model) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n비회원은 로그인 후 이용해주시기 바랍니다.");
				// 메인 페이지로 이동
				return "Page/main";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 메인 페이지로 이동
				return "Page/main";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 메인 페이지로 이동
			return "Page/main";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 메인 페이지로 이동
				return "Page/main";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 메인 페이지로 이동
					return "Page/main";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);
		
		// 그 다음 idx에 해당하는 사진첩의 모든 게시물 조회
		List<Gallery> galleryList = galleryService.findByGalleryIdxOrderByIdxDesc(idx);
		// 조회된 모든 게시물을 리스트 형태로 바인딩
		model.addAttribute("galleryList", galleryList);
		
		// 그 다음 idx에 해당하는 사진첩의 모든 댓글 조회
		List<GalleryComment> commentList = galleryService.findByGalleryCommentIdxOrderByIdxDesc(idx);
		// 조회된 모든 댓글을 리스트 형태로 바인딩
		model.addAttribute("commentList", commentList);
		
		// 그 다음 idx에 해당하는 유저정보를 조회
		Sign sign = signService.findByIdx(idx);
		// 조회된 유저정보를 바인딩
		model.addAttribute("sign", sign);
		// 로그인 유저 idx를 바인딩
		model.addAttribute("loginIdx", loginIdx);
		
		// 그 다음 사진첩에 댓글 작성자를 만들기 위해 로그인 유저 idx에 해당하는 유저정보를 조회
		Sign loginUser = signService.findByIdx(loginIdx);
		
		// 조회한 유저정보에서 댓글 작성자를 만들어 담을 String변수
		String galleryCommentName = "";

		/* 이메일 @부분까지 잘라낸 뒤 플랫폼명 추가 - 폐기
		 * 네이버 - qwer@ + naver = qwer@naver
		 * 카카오 - qwer@ + kakao = qwer@kakao
		 */
		// galleryCommentName = (loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") + 1 ) + loginUser.getPlatform());

		/* 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
		 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
		 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
		 */
		galleryCommentName = ( "( " + loginUser.getName() + " / " + loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") ) + " )" );

		// 만들어진 댓글 작성자를 바인딩
		model.addAttribute("loginName", galleryCommentName);
		
		// 사진첩 페이지로 이동
		return "Page/Gallery/gallery_list";
	}
	
	// 사진첩 글 작성 페이지로 이동
	@RequestMapping("/gallery_insert_form")
	public String gallery_insert_form(int idx, Model model) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n비회원은 로그인 후 이용해주시기 바랍니다.");
				// 사진첩 페이지로 이동
				return "Page/Gallery/gallery_list";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 페이지로 이동
				return "Page/Gallery/gallery_list";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 페이지로 이동
			return "Page/Gallery/gallery_list";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 페이지로 이동
				return "Page/Gallery/gallery_list";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 사진첩 페이지로 이동
					return "Page/Gallery/gallery_list";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 게시글은 오로지 미니홈피 주인만 작성할 수 있다.
		if ( loginIdx != idx ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 페이지로 이동
			return "Page/Gallery/gallery_list";
		}

		// 게시글 DTO 생성
		GalleryDTO galleryDTO = new GalleryDTO();
		// 미니홈피 유저 idx 지정
		galleryDTO.setGalleryIdx(idx);

		// 게시글 DTO 바인딩
		model.addAttribute("galleryDTO", galleryDTO);
		
		// 사진첩 작성 페이지로 이동
		return "Page/Gallery/gallery_insert_form";
	}
	
	// 사진첩 새 글 작성
	@RequestMapping("/gallery_insert")
	public String insert(GalleryDTO galleryDTO, Model model) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n비회원은 로그인 후 이용해주시기 바랍니다.");
				// 사진첩 작성 페이지로 이동
				return "Page/Gallery/gallery_insert_form";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 작성 페이지로 이동
				return "Page/Gallery/gallery_insert_form";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 작성 페이지로 이동
			return "Page/Gallery/gallery_insert_form";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 작성 페이지로 이동
				return "Page/Gallery/gallery_insert_form";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 사진첩 작성 페이지로 이동
					return "Page/Gallery/gallery_insert_form";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 게시글은 오로지 미니홈피 주인만 작성할 수 있다.
		if ( loginIdx != galleryDTO.getGalleryIdx() ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 작성 페이지로 이동
			return "Page/Gallery/gallery_insert_form";
		}
		
		// 클라이언트의 파일 업로드를 위해 절대 경로를 생성
		String savePath = "/Users/p._.sc/IT/ToyProject/CyworldProject/util/files/gallery"; // 절대 경로
		// 업로드를 위해 파라미터로 넘어온 파일의 정보
		MultipartFile galleryFile = galleryDTO.getGalleryFile();
		// 업로드된 파일이 없을 경우 파일 이름 지정
		galleryDTO.setGalleryFileName("no_file");
		// 업로드된 파일이 없는 경우 확장자에 파일 없음 지정
		galleryDTO.setGalleryFileExtension("no_file");
		
		// 업로드된 파일이 있을 경우
		if ( !galleryFile.isEmpty() ) {
			// 업로드된 파일의 원본 이름을 지정
			String galleryFileName = galleryFile.getOriginalFilename();
			/* 확장자 구하기
			 * 파일 원본 이름에서 마지막 .이 들어간 위치에서 한 칸 더한 위치부터 끝까지 잘라내기
			 * ex) 19292930388시바견.img --> .img --> img
			 */
			String galleryFileExtension = galleryFileName.substring( galleryFileName.lastIndexOf( "." ) + 1 );
			// 파일 종류가 비디오일 경우
			if ( galleryFileExtension.equals("mp4") ) {
				// 사진 이름 중복 및 한글 깨짐 방지를 위해 사진 이름을 시간으로 변경
				long time = System.currentTimeMillis();
				galleryFileName = String.format("%d.%s", time, galleryFileExtension);
				// 파일 이름 지정
				galleryDTO.setGalleryFileName(galleryFileName);
				// 확장자에 비디오 지정
				galleryDTO.setGalleryFileExtension("video");
			// 파일 종류가 이미지일 경우
			} else if ( galleryFileExtension.equals("jpg") || galleryFileExtension.equals("jpeg") || galleryFileExtension.equals("png") ) {
				// 사진 이름 중복 및 한글 깨짐 방지를 위해 사진 이름을 시간으로 변경
				long time = System.currentTimeMillis();
				galleryFileName = String.format("%d.%s", time, galleryFileExtension);
				// 파일 이름 지정
				galleryDTO.setGalleryFileName(galleryFileName);
				// 확장자에 이미지 지정
				galleryDTO.setGalleryFileExtension("image");
			// 업로드된 파일의 종류가 사용할 수 없는 경우
			} else {
				// 파일 이름 지정
				galleryDTO.setGalleryFileName("no_file");
				// 확장자에 파일 없음 지정
				galleryDTO.setGalleryFileExtension("no_file");
			}
			// 파일을 저장할 경로를 지정
			File saveFile = new File(savePath, galleryFileName);
			// 저장할 경로가 없을 경우
			if( !saveFile.exists() ) {
				// 경로를 생성
				saveFile.mkdirs();
			}
			// 업로드된 파일을 실제로 저장 - try~catch 필요
			try {
				// 업로드된 파일을 실제로 저장해 주는 메소드
				galleryFile.transferTo(saveFile);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 작성한 게시글을 저장
		galleryService.insertIntoGallery(galleryDTO.toInsertEntity());

		// idx를 들고 사진첩 페이지 URL로 이동
		return "redirect:/gallery/" + galleryDTO.getGalleryIdx();
	}
	
	// 사진첩 글 삭제
	@RequestMapping("/gallery_delete")
	@ResponseBody // Ajax로 요청된 메서드는 결과를 콜백 메서드로 돌려주기 위해 반드시 @ResponseBody가 필요!!
	public String delete(Gallery gallery) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 코드를 반환한다.
				return "0";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 코드를 반환한다.
				return "-4";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 코드를 반환한다.
			return "-99";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 코드를 반환한다.
				return "-1";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 코드를 반환한다.
					return "-100";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 게시글은 오로지 미니홈피 주인만 삭제할 수 있다.
		if ( loginIdx != gallery.getGalleryIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 삭제 실패할 경우
		String result = "no";
		
		// DB에 저장된 게시글 중 가져온 정보에 해당하는 게시글 삭제
		int res = galleryService.deleteByGalleryIdxAndIdx(gallery);
		if (res == 1) {
			// 삭제 성공할 경우
			result = "yes";
		}
		
		// 콜백 메소드에 전달
		return result;
	}
	
	// 사진첩 게시글 수정 페이지로 이동
	@RequestMapping("/gallery_modify_form")
	public String modify_form(Gallery gallery, Model model) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n비회원은 로그인 후 이용해주시기 바랍니다.");
				// 사진첩 페이지로 이동
				return "Page/Gallery/gallery_list";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 페이지로 이동
				return "Page/Gallery/gallery_list";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 페이지로 이동
			return "Page/Gallery/gallery_list";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 페이지로 이동
				return "Page/Gallery/gallery_list";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 사진첩 페이지로 이동
					return "Page/Gallery/gallery_list";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 게시글은 오로지 미니홈피 주인만 수정할 수 있다.
		if ( loginIdx != gallery.getGalleryIdx() ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 페이지로 이동
			return "Page/Gallery/gallery_list";
		}
		
		// 해당 idx의 사진첩에 수정할 게시글을 조회
		Gallery updateGallery = galleryService.findByGalleryIdxAndIdx(gallery);
		if ( updateGallery != null ) {
			// 게시글 DTO에 수정할 게시글 정보 전달
			GalleryDTO galleryDTO = new GalleryDTO(updateGallery);
			// 게시글 DTO 바인딩
			model.addAttribute("galleryDTO", galleryDTO);
		}
		
		// 사진첩 수정 페이지로 이동
		return "Page/Gallery/gallery_modify_form";
	}
	
	// 게시글 수정하기
	@RequestMapping("/gallery_modify")
	public String modify(GalleryDTO galleryDTO, Model model) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n비회원은 로그인 후 이용해주시기 바랍니다.");
				// 사진첩 수정 페이지로 이동
				return "Page/Gallery/gallery_modify_form";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 수정 페이지로 이동
				return "Page/Gallery/gallery_modify_form";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 수정 페이지로 이동
			return "Page/Gallery/gallery_modify_form";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 사진첩 수정 페이지로 이동
				return "Page/Gallery/gallery_modify_form";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 사진첩 수정 페이지로 이동
					return "Page/Gallery/gallery_modify_form";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 게시글은 오로지 미니홈피 주인만 수정할 수 있다.
		if ( loginIdx != galleryDTO.getGalleryIdx() ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 사진첩 수정 페이지로 이동
			return "Page/Gallery/gallery_modify_form";
		}

		// 클라이언트의 파일 업로드를 위해 절대 경로를 생성
		String savePath = "/Users/p._.sc/IT/ToyProject/CyworldProject/util/files/gallery"; // 절대 경로
		// 업로드를 위해 파라미터로 넘어온 파일의 정보
		MultipartFile galleryFile = galleryDTO.getGalleryFile();
		
		// 업로드된 파일이 있을 경우
		if ( !galleryFile.isEmpty() ) {
//			// 이미 저장되어 있는 파일 삭제
//			File delFile = new File(savePath, galleryDTO.getGalleryFileName());
//			delFile.delete();
			// 업로드된 파일의 원본 이름을 지정
			String galleryFileName = galleryFile.getOriginalFilename();
			/* 확장자 구하기
			 * 파일 원본 이름에서 마지막 .이 들어간 위치에서 한 칸 더한 위치부터 끝까지 잘라내기
			 * ex) 19292930388시바견.img --> .img --> img
			 */
			String galleryFileExtension = galleryFileName.substring( galleryFileName.lastIndexOf( "." ) + 1 );
			// 파일 종류가 비디오일 경우
			if ( galleryFileExtension.equals("mp4") ) {
				// 사진 이름 중복 및 한글 깨짐 방지를 위해 사진 이름을 시간으로 변경
				long time = System.currentTimeMillis();
				galleryFileName = String.format("%d.%s", time, galleryFileExtension);
				// 파일 이름 지정
				galleryDTO.setGalleryFileName(galleryFileName);
				// 확장자에 비디오 지정
				galleryDTO.setGalleryFileExtension("video");
			// 파일 종류가 이미지일 경우
			} else if ( galleryFileExtension.equals("jpg") || galleryFileExtension.equals("jpeg") || galleryFileExtension.equals("png") ) {
				// 사진 이름 중복 및 한글 깨짐 방지를 위해 사진 이름을 시간으로 변경
				long time = System.currentTimeMillis();
				galleryFileName = String.format("%d.%s", time, galleryFileExtension);
				// 파일 이름 지정
				galleryDTO.setGalleryFileName(galleryFileName);
				// 확장자에 이미지 지정
				galleryDTO.setGalleryFileExtension("image");
			// 업로드된 파일의 종류가 사용할 수 없는 경우
			} else {
				// 파일 이름 지정
				galleryDTO.setGalleryFileName("no_file");
				// 확장자에 파일 없음 지정
				galleryDTO.setGalleryFileExtension("no_file");
			}
			// 파일을 저장할 경로를 지정
			File saveFile = new File(savePath, galleryFileName);
			// 저장할 경로가 없을 경우
			if( !saveFile.exists() ) {
				// 경로를 생성
				saveFile.mkdirs();
			}
			// 업로드된 파일을 실제로 등록 - try~catch 필요
			try {
				// 업로드된 파일을 실제로 등록해 주는 메소드
				galleryFile.transferTo(saveFile);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// 수정된 파일 및 게시글로 갱신
		galleryService.updateSetGalleryTitleAndGalleryRegDateAndGalleryContentAndGalleryFileNameAndGalleryFileExtensionByGalleryIdxAndIdx(galleryDTO.toModifyEntity());
		
		// idx를 들고 사진첩 페이지 URL로 이동
		return "redirect:/gallery/" + galleryDTO.getGalleryIdx();
	}
	
	/////////////// 사진첩 댓글 구역 ///////////////
	
	// 댓글 작성
	@RequestMapping("/comment_insert")
	@ResponseBody
	public String gallery_reply(GalleryComment galleryComment) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 코드를 반환한다.
				return "0";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 코드를 반환한다.
				return "-4";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 코드를 반환한다.
			return "-99";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 코드를 반환한다.
				return "-1";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 코드를 반환한다.
					return "-100";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}

		// 토큰에서 추출한 로그인 유저 idx와 댓글에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != galleryComment.getGalleryCommentSessionIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 작성 시간을 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 댓글 idx에 AUTO_INCREMENT로 null 지정
		galleryComment.setIdx(null);
		// 댓글의 삭제 여부 0 지정
		galleryComment.setGalleryCommentDeleteCheck(0);
		// 댓글에 작성 시간 지정
		galleryComment.setGalleryCommentRegDate(today.format(date));

		// 저장 실패할 경우
		String result = "no";

		// 작성한 댓글을 저장
		GalleryComment res = galleryService.insertIntoGalleryComment(galleryComment);
		if ( res != null ) {
			// 저장 성공할 경우
			result = "yes";
		}

		// 콜백 메소드에 전달
		return result;
	}
	
	// 댓글 삭제 - 완전 삭제가 아닌 삭제된 것처럼 만들기
	@RequestMapping("/gcomment_delete")
	@ResponseBody
	public String comment_delete(GalleryComment galleryComment) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 에러 코드를 반환한다.
				return "0";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 코드를 반환한다.
				return "-4";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 코드를 반환한다.
			return "-99";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 에러 코드를 반환한다.
				return "-1";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 코드를 반환한다.
					return "-100";
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 댓글은 미니홈피 주인 혹은 작성자만 삭제할 수 있다.
		if ( loginIdx != galleryComment.getGalleryCommentIdx() && loginIdx != galleryComment.getGalleryCommentSessionIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 댓글의 삭제 여부 -1 지정
		galleryComment.setGalleryCommentDeleteCheck(-1);

		// 삭제 실패할 경우
		String result = "no";

		// 삭제할 댓글의 삭제 여부만 갱신
		int res = galleryService.updateSetGalleryCommentDeleteCheckByGalleryCommentIdxAndGalleryIdxCommentAndIdx(galleryComment);
		if (res == 1) {
			// 삭제 성공할 경우
			result = "yes";
		}

		// 콜백 메소드에 전달
		return result;
	}
	
	/////////////// 사진첩 좋아요 구역 ///////////////
	
	@RequestMapping("/gallery_like")
	@ResponseBody
	public Gallery gallery_like(Gallery gallery, GalleryLike galleryLike) {
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키에 저장한 토큰을 가져온다.
					authorization = cookie.getValue();
					// foreach문을 빠져나간다.
					break;
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 비회원 or 에러
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 게시글 내용에 에러 코드 지정
				gallery.setGalleryContent("0");
				// 갱신된 게시글 전달
				return gallery;
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 게시글 내용에 에러 코드 지정
				gallery.setGalleryContent("-4");
				// 갱신된 게시글 전달
				return gallery;
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 게시글 내용에 에러 코드 지정
			gallery.setGalleryContent("-99");
			// 갱신된 게시글 전달
			return gallery;
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 게시글 내용에 에러 코드 지정
				gallery.setGalleryContent("-1");
				// 갱신된 게시글 전달
				return gallery;
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 게시글 내용에 에러 코드 지정
					gallery.setGalleryContent("-100");
					// 갱신된 게시글 전달
					return gallery;
				// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
				} else {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// Authorization 쿠키에 재생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", refreshToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(refreshToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 재생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// JWT에서 재생성한 토큰에 해당하는 로그인 유저 idx를 추출한다.
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}

		// 토큰에서 추출한 로그인 유저 idx와 좋아요에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != galleryLike.getGalleryLikeSessionIdx() ) {
			// 게시글 내용에 에러 코드 지정
			gallery.setGalleryContent("-4");
			// 갱신된 게시글 전달
			return gallery;
		}

		// 미니홈피 유저 idx 지정
		gallery.setGalleryIdx(galleryLike.getGalleryLikeIdx());
		// 좋아요를 누른 게시글의 번호 지정
		gallery.setIdx(galleryLike.getGalleryLikeRef());

		// 먼저 DB에 로그인한 유저가 해당 idx의 사진첩 게시글에 좋아요를 눌렀는지 조회
		GalleryLike galleryLikeCheck = galleryService.findByGalleryLikeIdxAndGalleryLikeRefAndGalleryLikeSessionIdx(galleryLike);

		// 이미 좋아요를 눌렀을 경우
		if ( galleryLikeCheck != null ) {
			// 이미 눌린 좋아요를 다시 누를 경우 취소되므로 좋아요 내역을 삭제
			galleryService.deleteByGalleryLikeIdxAndGalleryIdxLikeAndGalleryLikeSessionIdx(galleryLike);
			// 좋아요 개수 조회
			int likeCount = galleryService.countByGalleryLikeIdxAndGalleryLikeRef(galleryLike);
			// 조회된 좋아요 개수를 지정
			gallery.setGalleryLikeNum(likeCount);
			// 조회된 좋아요 개수로 갱신
			galleryService.updateSetGalleryLikeNumByGalleryIdxIdxAndIdx(gallery);
			// 콜백 메소드에 갱신된 게시글 전달
			return gallery;
		// 좋아요를 안 눌렀을 경우
		} else {
			// 좋아요 idx에 AUTO_INCREMENT로 null 지정
			galleryLike.setIdx(null);
			// 좋아요를 누를 경우 좋아요 내역을 추가
			galleryService.insertIntoGalleryLike(galleryLike);
			// 좋아요 개수 조회
			int likeCount = galleryService.countByGalleryLikeIdxAndGalleryLikeRef(galleryLike);
			// 조회된 좋아요 개수를 지정
			gallery.setGalleryLikeNum(likeCount);
			// 조회된 좋아요 개수로 갱신
			galleryService.updateSetGalleryLikeNumByGalleryIdxIdxAndIdx(gallery);
			// 콜백 메소드에 갱신된 게시글 전달
			return gallery;
		}
	}
}