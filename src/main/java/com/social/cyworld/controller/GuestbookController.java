package com.social.cyworld.controller;

import com.social.cyworld.dto.UserDTO;
import com.social.cyworld.entity.*;
import com.social.cyworld.service.GuestbookService;
import com.social.cyworld.service.MainService;
import com.social.cyworld.service.SignService;
import com.social.cyworld.service.UserDTOService;
import com.social.cyworld.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RequestMapping("/guestbook")
@Controller
public class GuestbookController {
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
	MainService mainService;
	@Autowired
	GuestbookService guestbookService;
	@Autowired
	UserDTOService userDTOService;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 방명록 조회
	@RequestMapping("/{idx}") // 경로 매개변수
	public String guestbook_list (@PathVariable int idx, Model model) {
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

		// 조회수 구역 시작 //

		// 접속 날짜를 기록하기 위해 Date 객체를 생성한다.
		Date date = new Date();
		// Date 객체를 원하는 형식대로 포맷한다.
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");

		// 방문 기록 조회용 Map을 생성한다.
		HashMap<String, Object> todayMap = new HashMap<String, Object>();
		todayMap.put("1", idx); // 미니홈피 유저 idx
		todayMap.put("2", loginIdx); // 로그인 유저 idx
		todayMap.put("3", today.format(date)); // 접속 날짜

		// 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 타 유저 미니홈피 조회 - 조회수 증가 O
		if ( loginIdx != idx ) {

			// 로그인 유저가 해당 미니홈피에 방문 기록이 있는지 조회한다.
			Views loginUser = mainService.findByViewsIdxAndViewsSessionIdx(todayMap);

			// 미니홈피 유저 idx에 해당하는 유저 메인 정보를 조회한다.
			UserMain miniUser = signService.findUserMainBySignIdx(idx);

			// 방문 기록이 있는 경우
			if ( loginUser != null ) {

				// 방문 기록 중 방문 날짜가 현재 날짜와 다른 경우
				if ( !loginUser.getTodayDate().equals(today.format(date)) ) {

					// 방문 기록 중 방문 날짜를 현재 날짜로 갱신한다.
					mainService.updateSetTodayDateByViewsIdxAndViewsSessionIdx(todayMap);

					// 조회한 유저 메인 정보 중 접속 날짜가 현재 날짜와 다른 경우
					if ( !miniUser.getToDate().equals(today.format(date)) ) {

						// 누적 조회수에 일일 조회수를 더하여 setter를 통해 전달한다.
						miniUser.setTotal(miniUser.getTotal() + miniUser.getToday());
						// 일일 조회수에 0으로 초기화 후 1 증가시켜 setter를 통해 전달한다.
						miniUser.setToday(1);
						// 접속 날짜에 현재 날짜를 setter를 통해 전달한다.
						miniUser.setToDate(today.format(date));
						// 전달받은 방문 기록으로 미니홈피 유저 메인 정보를 갱신한다.
						signService.updateSetTodayAndTotalAndToDateByIdx(miniUser);

					// 조회한 유저 메인 정보 중 접속 날짜가 현재 날짜와 같은 경우
					} else {

						// 일일 조회수에 1 증가시켜 setter를 통해 전달한다.
						miniUser.setToday(miniUser.getToday() + 1);
						// 전달받은 일일 조회수로 미니홈피 유저 메인 정보를 갱신한다.
						signService.updateSetTodayByIdx(miniUser);

					}

				// 방문 기록 중 방문 날짜가 현재 날짜와 같은 경우
				} else {

					// 조회수를 증가시키지 않고 통과

				}

			// 방문 기록이 없는 경우
			} else {

				// 방문 기록 Entity를 생성한다.
				Views views = new Views();
				// 방문 기록 idx에 AUTO_INCREMENT로 null을 설정하여 setter를 통해 전달한다.
				views.setIdx(null);
				// 방문 기록 viewsIdx에 미니홈피 유저 idx를 setter를 통해 전달한다.
				views.setViewsIdx(idx);
				// 방문 기록 sessionIdx에 로그인 유저 idx를 setter를 통해 전달한다.
				views.setViewsSessionIdx(loginIdx);
				// 방문 기록 날짜에 현재 날짜를 setter를 통해 전달한다.
				views.setTodayDate(today.format(date));
				// 전달받은 방문 기록을 저장한다.
				mainService.insertIntoViews(views);

				// 조회한 유저 메인 정보 중 접속 날짜가 현재 날짜와 다른 경우
				if ( !miniUser.getToDate().equals(today.format(date)) ) {

					// 누적 조회수에 일일 조회수를 더하여 setter를 통해 전달한다.
					miniUser.setTotal(miniUser.getTotal() + miniUser.getToday());
					// 일일 조회수에 0으로 초기화 후 1 증가시켜 setter를 통해 전달한다.
					miniUser.setToday(1);
					// 접속 날짜에 현재 날짜를 setter를 통해 전달한다.
					miniUser.setToDate(today.format(date));
					// 전달받은 방문 기록으로 미니홈피 유저 메인 정보를 갱신한다.
					signService.updateSetTodayAndTotalAndToDateByIdx(miniUser);

				// 조회한 유저 메인 정보 중 접속 날짜가 현재 날짜와 같은 경우
				} else {

					// 일일 조회수에 1 증가시켜 setter를 통해 전달한다.
					miniUser.setToday(miniUser.getToday() + 1);
					// 전달받은 일일 조회수로 미니홈피 유저 메인 정보를 갱신한다.
					signService.updateSetTodayByIdx(miniUser);

				}

			}

		// 로그인 유저 idx와 미니홈피 유저 idx가 같은 경우 - 내 미니홈피 조회 - 조회수 증가 X
		} else {

			// 로그인 유저 idx에 해당하는 유저 메인 정보를 조회한다.
			UserMain myMini = signService.findUserMainBySignIdx(loginIdx);

			// 조회한 유저 메인 정보 중 접속 날짜가 현재 날짜와 다른 경우
			if ( !myMini.getToDate().equals(today.format(date)) ) {

				// 누적 조회수에 일일 조회수를 더하여 setter를 통해 전달한다.
				myMini.setTotal(myMini.getTotal() + myMini.getToday());
				// 일일 조회수에 0으로 초기화시켜 setter를 통해 전달한다.
				myMini.setToday(0);
				// 접속 날짜에 현재 날짜를 setter를 통해 전달한다.
				myMini.setToDate(today.format(date));
				// 전달받은 방문 기록으로 로그인 유저 메인 정보를 갱신한다.
				signService.updateSetTodayAndTotalAndToDateByIdx(myMini);

			// 조회한 유저 메인 정보 중 접속 날짜가 현재 날짜와 같은 경우
			} else {

				// 조회수를 증가시키지 않고 통과

			}

		}

		// 조회수 구역 끝 //

		// 미니홈피 유저 idx에 해당하는 방명록 방문글을 모두 조회하여 리스트로 가져온다.
		List<Guestbook> list = guestbookService.findByGuestbookIdxOrderByIdxDesc(idx);
		// 가져온 방명록 방문글 리스트를 바인딩한다.
		model.addAttribute("list", list);
		
		// 로그인 유저 idx에 해당하는 방명록 페이지 유저 정보를 조회한다.
		UserDTO userDTO = userDTOService.findGuestbookByIdx(idx);
		// 조회한 방명록 페이지 유저 정보 DTO를 바인딩한다.
		model.addAttribute("sign", userDTO);
		// 로그인 유저 idx를 바인딩한다.
		model.addAttribute("loginIdx", loginIdx);

		// 일촌 Entity를 생성한다.
		Ilchon ilchon = new Ilchon();
		// 일촌 상태에 맞일촌을 의미하는 2를 setter를 통해 전달한다.
		ilchon.setIlchonUp(2);
		// 일촌 idx에 로그인 유저 idx를 setter를 통해 전달한다.
		ilchon.setIlchonSessionIdx(loginIdx);
		// 전달받은 맞일촌에 해당하는 일촌을 모두 조회하여 리스트로 가져온다.
		List<Ilchon> ilchonList = mainService.findByIlchonSessionIdxAndIlchonUp(ilchon);
		// 가져온 일촌 리스트를 바인딩한다.
		model.addAttribute("ilchonList", ilchonList);
		
		// 방명록 페이지로 이동
		return "Page/Guestbook/guestbook_list";
	}
	
	// 방명록 방문글 작성 페이지로 이동
	@RequestMapping("/guestbook_insert_form")
	public String guestbook_insert_form(int idx, Model model) {
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
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 페이지로 이동
			return "Page/Guestbook/guestbook_list";
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
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 방명록 페이지로 이동
					return "Page/Guestbook/guestbook_list";
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
		
		// 방명록에 작성자 정보를 저장하기 위해 로그인 유저 idx에 해당하는 유저 정보를 조회한다.
		UserDTO loginUser = userDTOService.findSignJoinUserProfileJoinUserMainByIdx(loginIdx);
		
		// 방명록 방문글 Entity를 생성한다.
		Guestbook guestbook = new Guestbook();
		// 방명록 방문글 guestbookIdx에 미니홈피 유저 idx를 setter를 통해 전달한다.
		guestbook.setGuestbookIdx(idx);
		// 방명록 방문글 guestbookSessionIdx에 로그인 유저 idx를 setter를 통해 전달한다.
		guestbook.setGuestbookSessionIdx(loginIdx);
		// 방명록 방문글 미니미에 조회한 유저 정보 중 미니미를 가져와 setter를 통해 전달한다.
		guestbook.setGuestbookMinimi(loginUser.getMinimi());
		/* 이메일 @부분까지 잘라낸 뒤 플랫폼명 추가 - 폐기
		 * 네이버 - qwer@ + naver = qwer@naver
		 * 카카오 - qwer@ + kakao = qwer@kakao
		 */
		// 방명록 방문글 작성자에 조회한 유저 프로필 정보 중 이메일과 플랫폼을 사용하여 작성자 이름을 만들어 setter를 통해 전달한다.
		// guestbook.setGuestbookContentName(loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") + 1 ) + loginUser.getPlatform());
		/* 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
		 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
		 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
		 */
		// 방명록 방문글 작성자에 조회한 유저 프로필 정보 중 이름과 이메일을 사용하여 작성자 이름을 만들어 setter를 통해 전달한다.
		guestbook.setGuestbookName("( " + loginUser.getName() + " / " + loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") ) + " )");

		// 전달받은 방명록 방문글 작성자 정보를 바인딩한다.
		model.addAttribute("guestbook", guestbook);

		// 방명록 작성 페이지로 이동
		return "Page/Guestbook/guestbook_insert_form";
	}
	
	// 방명록 새 방문글 작성
	@RequestMapping("/guestbook_insert")
	public String guestbook_insert(Guestbook guestbook, Model model) {
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
				// 방명록 작성 페이지로 이동
				return "Page/Guestbook/guestbook_insert_form";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 방명록 작성 페이지로 이동
				return "Page/Guestbook/guestbook_insert_form";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 작성 페이지로 이동
			return "Page/Guestbook/guestbook_insert_form";
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
				// 방명록 작성 페이지로 이동
				return "Page/Guestbook/guestbook_insert_form";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 방명록 작성 페이지로 이동
					return "Page/Guestbook/guestbook_insert_form";
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

		// 토큰에서 추출한 로그인 유저 idx와 방문글에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 작성 페이지로 이동
			return "Page/Guestbook/guestbook_insert_form";
		}

		// 작성 시간을 기록하기 위해 Date 객체를 생성한다.
		Date date = new Date();
		// Date 객체를 원하는 형식대로 포맷한다.
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 방명록 방문글 idx에 AUTO_INCREMENT로 null을 설정하여 setter를 통해 전달한다.
		guestbook.setIdx(null);
		// 방명록 방문글 좋아요에 시작 개수 0을 setter를 통해 전달한다.
		guestbook.setGuestbookLikeNum(0);
		// 방명록 방문글 작성 시간에 Date 객체로 만든 작성 시간을 가져와 setter를 통해 전달한다.
		guestbook.setGuestbookRegDate(today.format(date));

		// 전달받은 방명록 작성 방문글을 저장한다.
		guestbookService.insertIntoGuestbook(guestbook);

		// idx를 들고 방명록 페이지 URL로 이동
		return "redirect:/guestbook/" + guestbook.getGuestbookIdx();
	}
	
	// 방명록 방문글 삭제
	@RequestMapping("/guestbook_delete")
	@ResponseBody // Ajax로 요청된 메서드는 결과를 콜백 메서드로 돌아가기 위해 반드시 필요한 어노테이션
	public String guestbook_delete(Guestbook guestbook) {
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

		// 토큰에서 추출한 로그인 유저 idx가 미니홈피 유저 idx와도 다르고 방문글에서 가져온 로그인 유저 idx와도 다른 경우 - 방문글은 미니홈피 주인 혹은 작성자만 삭제할 수 있다.
		if ( loginIdx != guestbook.getGuestbookIdx() && loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 삭제 실패하는 경우
		String result = "no";

		// 방명록 방문글 정보에 해당하는 방명록 작성 방문글을 삭제한다.
		int res = guestbookService.deleteByGuestbookIdxAndIdx(guestbook);
		if ( res == 1 ) {
			// 삭제 성공하는 경우
			result = "yes";
		}

		// 결과 메시지 전달
		return result;
	}
	
	// 방명록 방문글 수정 페이지로 이동
	@RequestMapping("/guestbook_modify_form")
	public String guestbook_modify_form(Guestbook guestbook, Model model) {
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
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 페이지로 이동
			return "Page/Guestbook/guestbook_list";
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
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 방명록 페이지로 이동
					return "Page/Guestbook/guestbook_list";
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

		// 토큰에서 추출한 로그인 유저 idx와 방문글에서 가져온 로그인 유저 idx가 다른 경우 - 방문글은 오로지 작성자만 수정할 수 있다.
		if ( loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 페이지로 이동
			return "Page/Guestbook/guestbook_list";
		}

		// 방명록에 작성자 정보를 수정하기 위해 로그인 유저 idx에 해당하는 유저 메인 정보를 조회한다.
		UserMain loginUser = signService.findUserMainBySignIdx(loginIdx);

		// 방명록 방문글 정보에 해당하는 방명록 작성 방문글을 조회한다.
		Guestbook updateGuestbook = guestbookService.findByGuestbookIdxAndIdx(guestbook);

		// 조회한 방명록 작성 방문글이 없는 경우
		if ( updateGuestbook == null ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("updateErrMsg", "수정하려는 글 정보가 없습니다.\n새로고침 후 다시 시도해주시기 바랍니다.");
			// 방명록 페이지로 이동
			return "Page/Guestbook/guestbook_list";
		}

		// 조회한 방명록 작성 방문글이 있는 경우

		// 조회한 방명록 작성 방문글 미니미에 조회한 유저 메인 정보 중 미니미를 가져와 setter를 통해 전달한다.
		updateGuestbook.setGuestbookMinimi(loginUser.getMinimi());

		// 전달받은 방명록 작성 방문글을 바인딩한다.
		model.addAttribute("updateGuestbook", updateGuestbook);

		// 방명록 수정 페이지로 이동
		return "Page/Guestbook/guestbook_modify_form";
	}
	
	// 방문글 수정하기
	@RequestMapping("/guestbook_modify")
	@ResponseBody
	public String guestbook_modify(Guestbook guestbook) {
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

		// 토큰에서 추출한 로그인 유저 idx와 방문글에서 가져온 로그인 유저 idx가 다른 경우 - 방문글은 오로지 작성자만 수정할 수 있다.
		if ( loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 수정 시간을 기록하기 위해 Date 객체를 생성한다.
		Date date = new Date();
		// Date 객체를 원하는 형식대로 포맷한다.
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 방명록 방문글 작성 시간에 Date 객체로 만든 수정 시간을 가져와 setter를 통해 전달한다.
		guestbook.setGuestbookRegDate(today.format(date));

		// 갱신 실패하는 경우
		String result = "no";

		// 전달받은 방명록 수정 방문글로 갱신한다.
		int res = guestbookService.updateSetGuestbookNameAndGuestbookMinimiAndGuestbookRegDateAndGuestbookContentAndGuestbookSecretCheckByGuestbookIdxAndGuestbookSessionIdxAndIdx(guestbook);
		if (res != 0) {
			// 갱신 성공하는 경우
			result = "yes";
		}

		// 결과 메시지 전달
		return result;
	}
	
	/////////////// 방명록 좋아요 구역 ///////////////
	
	@RequestMapping("/guestbook_like")
	@ResponseBody
	public Guestbook guestbook_like(Guestbook guestbook, GuestbookLike guestbookLike) {
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
				// 방문글 내용에 에러 코드 지정
				guestbook.setGuestbookContent("0");
				// 갱신된 방문글 전달
				return guestbook;
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 방문글 내용에 에러 코드 지정
				guestbook.setGuestbookContent("-4");
				// 갱신된 방문글 전달
				return guestbook;
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 방문글 내용에 에러 코드 지정
			guestbook.setGuestbookContent("-99");
			// 갱신된 방문글 전달
			return guestbook;
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 방문글 내용에 에러 코드 지정
				guestbook.setGuestbookContent("-1");
				// 갱신된 방문글 전달
				return guestbook;
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 방문글 내용에 에러 코드 지정
					guestbook.setGuestbookContent("-100");
					// 갱신된 방문글 전달
					return guestbook;
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
		if ( loginIdx != guestbookLike.getGuestbookLikeSessionIdx() ) {
			// 방문글 내용에 에러 코드 지정
			guestbook.setGuestbookContent("-4");
			// 갱신된 방문글 전달
			return guestbook;
		}

		// 방명록 방문글 idx에 방명록 좋아요 정보 중 방문글 idx를 가져와 setter를 통해 전달한다.
		guestbook.setIdx(guestbookLike.getGuestbookLikeRef());
		// 방명록 방문글 guestbookIdx에 방명록 좋아요 정보 중 미니홈피 유저 idx를 가져와 setter를 통해 전달한다.
		guestbook.setGuestbookIdx(guestbookLike.getGuestbookLikeIdx());

		// 방명록 좋아요 정보에 해당하는 좋아요 기록이 존재하는지 조회하여 체크한다.
		GuestbookLike guestbookLikeCheck = guestbookService.findByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(guestbookLike);

		// 좋아요 기록이 존재하는 경우
		if ( guestbookLikeCheck != null ) {
			// 좋아요를 누른 상태에서 좋아요를 또 누르면 좋아요가 취소 되기에 방명록 좋아요 정보에 해당하는 좋아요 기록을 삭제한다.
			guestbookService.deleteByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(guestbookLike);
			// 방명록 좋아요 정보에 해당하는 방문글의 좋아요 개수를 COUNT로 조회한다.
			int likeCount = guestbookService.countByGuestbookLikeIdxAndGuestbookLikeRef(guestbookLike);
			// 방명록 방문글 좋아요 개수에 조회한 좋아요 개수를 setter를 통해 전달한다.
			guestbook.setGuestbookLikeNum(likeCount);
			// 전달받은 방명록 방문글 정보로 갱신한다.
			guestbookService.updateSetGuestbookLikeNumByGuestbookIdxAndIdx(guestbook);
			// 갱신한 방명록 방문글 정보를 전달
			return guestbook;
		// 좋아요 기록이 존재하지 않는 경우
		} else {
			// 방명록 좋아요 idx에 AUTO_INCREMENT로 null을 설정하여 setter를 통해 전달한다.
			guestbookLike.setIdx(null);
			// 좋아요를 안 누른 상태에서 좋아요를 누르면 좋아요가 추가 되기에 방명록 좋아요 정보를 저장한다.
			guestbookService.insertIntoGuestbookLike(guestbookLike);
			// 방명록 좋아요 정보에 해당하는 방문글의 좋아요 개수를 COUNT로 조회한다.
			int likeCount = guestbookService.countByGuestbookLikeIdxAndGuestbookLikeRef(guestbookLike);
			// 방명록 방문글 좋아요 개수에 조회한 좋아요 개수를 setter를 통해 전달한다.
			guestbook.setGuestbookLikeNum(likeCount);
			// 전달받은 방명록 방문글 정보로 갱신한다.
			guestbookService.updateSetGuestbookLikeNumByGuestbookIdxAndIdx(guestbook);
			// 갱신한 방명록 방문글 정보를 전달
			return guestbook;
		}
	}
}