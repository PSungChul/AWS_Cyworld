package com.social.cyworld.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.social.cyworld.dto.UserDTO;
import com.social.cyworld.entity.*;
import com.social.cyworld.httpclient.NicePay;
import com.social.cyworld.service.MainService;
import com.social.cyworld.service.ProductService;
import com.social.cyworld.service.SignService;
import com.social.cyworld.service.UserDTOService;
import com.social.cyworld.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	HttpServletResponse response;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	SignService signService;
	@Autowired
	MainService mainService;
	@Autowired
	ProductService productService;
	@Autowired
	UserDTOService userDTOService;

	// properties - NiCEPAY
	@Value("${payClientId:payClientId}")
	private String payClientId;
	@Value("${payClientSecret:payClientSecret}")
	private String payClientSecret;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 메인 페이지로 이동
	@RequestMapping("/main/{idx}") // 경로 매개변수
	public String main(@PathVariable int idx, Model model) {
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
				// 미니홈피 유저 idx에 해당하는 메인 페이지 유저 정보를 조회한다.
				UserDTO userDTO = userDTOService.findMainByIdx(idx);
				// 조회한 메인 페이지 유저 정보 DTO를 바인딩한다.
				model.addAttribute("sign", userDTO);

				// 미니홈피 유저 idx에 해당하는 일촌평을 모두 조회하여 리스트로 가져온다.
				List<Ilchonpyeong> ilchonpyeongList = mainService.findByIlchonpyeongIdx(idx);
				// 가져온 일촌평 리스트를 바인딩한다.
				model.addAttribute("ilchonpyeongList", ilchonpyeongList);

				// 비회원용으로 유저 정보 Entity를 생성한다.
				Sign loginUser = new Sign();
				// 유저 정보 idx에 비회원용 idx를 setter를 통해 전달한다.
				loginUser.setIdx(-1);
				// 전달받은 비회원용 유저 정보를 바인딩한다.
				model.addAttribute("loginUser", loginUser);

				// 비회원용으로 일촌 Entity 리스트를 생성한다.
				List<Ilchon> ilchonList = new ArrayList<>();
				// 비회원용 일촌 리스트를 바인딩한다.
				model.addAttribute("ilchonList", ilchonList);

				// 비회원용으로 일촌 Entity를 생성한다.
				Ilchon ilchonUser = new Ilchon();
				// 일촌 상태에 비회원을 의미하는 -1을 setter를 통해 전달한다.
				ilchonUser.setIlchonUp(-1);
				// 전달받은 비회원용 일촌 관계를 바인딩한다.
				model.addAttribute("ilchon", ilchonUser);

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

		// 미니홈피 유저 idx에 해당하는 메인 페이지 유저 정보를 조회한다.
		UserDTO userDTO = userDTOService.findMainByIdx(idx);
		// 조회한 메인 페이지 유저 정보 DTO를 바인딩한다.
		model.addAttribute("sign", userDTO);

		// 조회한 메인 페이지 유저 정보 중 암호화한 권한이 관리자 권한인지 유저 권한인지 체크한다.
		// 관리자 권한인 경우
		if (passwordEncoder.matches("ADMIN", userDTO.getRoles())) {
			// 조회한 메인 페이지 유저 정보 중 권한에 "ANMIN"을 setter을 통해 전달한다.
			userDTO.setRoles("ADMIN");
		// 유저 권한인 경우
		} else {
			// 조회한 메인 페이지 유저 정보 중 권한에 "USER"을 setter을 통해 전달한다.
			userDTO.setRoles("USER");
		}

		// 미니홈피 유저 idx에 해당하는 일촌평을 모두 조회하여 리스트로 가져온다.
		List<Ilchonpyeong> ilchonpyeongList = mainService.findByIlchonpyeongIdx(idx);
		// 가져온 일촌평 리스트를 바인딩한다.
		model.addAttribute("ilchonpyeongList", ilchonpyeongList);

		// 로그인 유저 idx에 해당하는 로그인 유저 정보를 조회한다.
		Sign loginUser = signService.findByIdx(loginIdx);
		// 유저 정보 DTO를 생성한다.
		UserDTO loginUserDTO = new UserDTO();
		// 조회한 로그인 유저 정보를 전달하여 로그인 유저 정보 DTO로 변환한다.
		loginUserDTO.toLoginUserDTO(loginUser);
		// 변환한 로그인 유저 정보 DTO를 바인딩한다.
		model.addAttribute("loginUser", loginUserDTO);

		// 조회한 로그인 유저 정보 중 암호화한 권한이 관리자 권한인지 유저 권한인지 체크한다.
		// 관리자 권한인 경우
		if (passwordEncoder.matches("ADMIN", loginUser.getRoles())) {
			// 변환한 로그인 유저 정보 DTO 중 권한에 "ANMIN"을 setter을 통해 전달한다.
			loginUserDTO.setRoles("ADMIN");
		// 유저 권한인 경우
		} else {
			// 변환한 로그인 유저 정보 DTO 중 권한에 "USER"을 setter을 통해 전달한다.
			loginUserDTO.setRoles("USER");
		}

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

		// 일촌 idx에 미니홈피 유저 idx를 setter를 통해 전달한다.
		ilchon.setIlchonIdx(idx);
		// 일촌 sessionIdx에 로그인 유저 idx를 setter를 통해 전달한다.
		ilchon.setIlchonSessionIdx(loginIdx);
		// 타 유저 미니홈피에 놀러갔을 때 해당 미니홈피 유저와의 일촌 관계를 조회한다.
		Ilchon ilchonUser = mainService.findByIlchonIdxAndIlchonSessionIdx(ilchon);
		// 아무 관계도 아닌 경우
		if ( ilchonUser == null ) {
			// 일촌 Entity를 생성한다.
			ilchonUser = new Ilchon();
			// 일촌 상태에 아무 관계도 아님을 의미하는 0을 setter를 통해 전달한다.
			ilchonUser.setIlchonUp(0);
		}
		// 조회한 일촌 관계를 바인딩한다.
		model.addAttribute("ilchon", ilchonUser);
		
		// 메인 페이지로 이동
		return "Page/main";
	}
	
	// 비회원 로그인
	@RequestMapping("/nmain")
	public String nmanin() {
		// 비회원용 세션이 존재하는지 체크한다.
		HttpSession session = request.getSession();
		// 비회원용 세션이 존재하지 않는 경우
		if ( session.getAttribute("login") == null ) {
			// 비회원용 세션으로 -1을 지정한다.
			session.setAttribute("login", -1);
		}

		// 비회원용 세션을 들고 메인페이지 이동
		return "Page/nmain";
	}

	/////////////// 검색 구역 ///////////////

	// 검색 팝업
	@RequestMapping("/main_search_popup")
	public String mainSearchPopup() {
		// 검색 팝업으로 이동
		return "Page/search_popup";
	}

	// 이름 및 ID 및 Email로 유저 검색
	@RequestMapping("/main_search")
	public String mainSearch(String searchType, String searchValue, Model model) {
		// 이름으로 검색한 경우
		if ( searchType.equals("name") ) {
			// 검색한 이름에 해당하는 모든 유저를 조회하여 리스트로 가져온다.
			List<UserDTO> list = userDTOService.findSignJoinUserProfileJoinUserMainByNameLike(searchValue);

			// 조회한 유저 리스트를 바인딩한다.
			model.addAttribute("list", list);
			// 추가로 검색 구분을 하기 위해 검색 타입도 바인딩한다.
			model.addAttribute("searchType", searchType);

			// 검색 팝업으로 이동
			return "Page/search_popup";
		// ID로 검색한 경우
		} else {
			// 검색한 아이디에 해당하는 모든 유저를 조회하여 리스트로 가져온다.
			List<UserDTO> list = userDTOService.findSignJoinUserProfileJoinUserMainByEmailLike(searchValue);

			// 조회한 유저 리스트를 바인딩한다.
			model.addAttribute("list", list);
			// 추가로 검색 구분을 하기 위해 검색 타입도 바인딩한다.
			model.addAttribute("searchType", searchType);

			// 검색 팝업으로 이동
			return "Page/search_popup";
		}
	}

	// 일촌평 작성
	@RequestMapping("/ilchonpyeong_write")
	@ResponseBody
	public String insert(Ilchonpyeong ilchonpyeong, int loginUserIdx) {
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

		// 토큰에서 추출한 로그인 유저 idx와 일촌평에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != loginUserIdx ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 일촌평에 작성자 이름을 만들기 위해 로그인 유저 idx에 해당하는 유저 프로필 정보를 조회한다.
		UserProfile loginUser = signService.findUserProfileBySignIdx(loginIdx);

		// 일촌평 idx에 AUTO_INCREMENT로 null을 설정하여 setter를 통해 전달한다.
		ilchonpyeong.setIdx(null);
		// 일촌평 작성자에 조회한 유저 프로필 정보 중 이메일과 플랫폼을 사용하여 작성자 이름을 만들어 setter를 통해 전달한다.
		/* 이메일 @부분까지 잘라낸뒤 플랫폼명 추가 - 폐기
		 * 네이버 - qwer@ + naver = qwer@naver
		 * 카카오 - qwer@ + kakao = qwer@kakao
		 */
		// ilchonpyeong.setIlchonpyeongSessionName(loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") + 1 ) + loginUser.getPlatform());
		// 일촌평 작성자에 조회한 유저 프로필 정보 중 이름과 이메일을 사용하여 작성자 이름을 만들어 setter를 통해 전달한다.
		/* 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
		 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
		 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
		 */
		ilchonpyeong.setIlchonpyeongSessionName("( " + loginUser.getName() + " / " + loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") ) + " )");

		// 저장 실패하는 경우
		String result = "no";

		// 전달받은 작성한 일촌평을 저장한다.
		Ilchonpyeong res = mainService.insertIntoIlchonpyeong(ilchonpyeong);
		if ( res != null ) {
			// 저장 성공하는 경우
			result = "yes";
		}

		// 결과 메시지 전달
		return result;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////관리자
	// 상품 추가 팝업
	@RequestMapping("/product/{adminIdx}")
	public String product(@PathVariable int adminIdx, Model model) {
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
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 상품 추가 팝업으로 이동
				return "Admin/add_product";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 상품 추가 팝업으로 이동
				return "Admin/add_product";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 상품 추가 팝업으로 이동
			return "Admin/add_product";
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
				// 상품 추가 팝업으로 이동
				return "Admin/add_product";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 상품 추가 팝업으로 이동
					return "Admin/add_product";
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
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx가 관리자 idx와 다르거나 관리자 idx가 아닌 경우 - 상품 추가 팝업은 오로지 관리자만 들어갈 수 있다.
		if ( loginIdx != adminIdx || loginIdx != 1 || adminIdx != 1 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 상품 추가 팝업으로 이동
			return "Admin/add_product";
		}

		// 로그인 유저 idx에 해당하는 유저 정보를 조회하여 암호화한 권한과 관리자 권한인 "ADMIN"이 일치하는지 체크한다. - 관리자 권한 검증
		Sign sign = signService.findByIdx(loginIdx);

		// 관리자 권한이 일치하지 않는 경우
		if (!passwordEncoder.matches("ADMIN", sign.getRoles())) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");

			// 상품 추가 팝업으로 이동
			return "Admin/add_product";
		}

		// 관리자 권한이 일치하는 경우

		// 관리자 idx를 바인딩한다.
		model.addAttribute("idx", loginIdx);

		// 상품 추가 팝업으로 이동
		return "Admin/add_product";
	}

	// 상품 추가
	@RequestMapping("/product/add")
	public String addProduct(int adminIdx, Product product, Model model) {
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
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 상품 추가 팝업으로 이동
				return "Admin/add_product";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 상품 추가 팝업으로 이동
				return "Admin/add_product";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 상품 추가 팝업으로 이동
			return "Admin/add_product";
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
				// 상품 추가 팝업으로 이동
				return "Admin/add_product";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 상품 추가 팝업으로 이동
					return "Admin/add_product";
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
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx가 관리자 idx와 다르거나 관리자 idx가 아닌 경우 - 상품 추가 팝업은 오로지 관리자만 들어갈 수 있다.
		if ( loginIdx != adminIdx || loginIdx != 1 || adminIdx != 1 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 상품 추가 팝업으로 이동
			return "Admin/add_product";
		}

		// 로그인 유저 idx에 해당하는 유저 정보를 조회하여 암호화한 권한과 관리자 권한인 "ADMIN"이 일치하는지 체크한다. - 관리자 권한 검증
		Sign sign = signService.findByIdx(loginIdx);

		// 관리자 권한이 일치하지 않는 경우
		if (!passwordEncoder.matches("ADMIN", sign.getRoles())) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");

			// 상품 추가 팝업으로 이동
			return "Admin/add_product";
		}

		// 관리자 권한이 일치하는 경우

		// 상품 Idx에 AUTO_INCREMENT로 null을 설정하여 setter를 통해 전달한다.
		product.setIdx(null);
		// 상품 번호에 상품 타입 + 상품 이름 + 상품 가격을 합쳐서 암호화하여 setter를 통해 전달한다.
		product.setProductIdx(passwordEncoder.encode(product.getProductType() + ":" + product.getName() + ":" + product.getPrice()));
		// 전달받은 상품 정보를 저장한다.
		productService.insertIntoProduct(product);

		// 상품 추가 팝업으로 이동
		return "redirect:/product/" + loginIdx;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////상품
	// 결제 및 구매하려는 상품 정보 조회
	@RequestMapping("/product_check")
	@ResponseBody
	public HashMap<String, Object> productCheck(int idx, String productIdx) {
		// 반환용 Map
		HashMap<String, Object> productMap = new HashMap<>();
		// 토큰 값
		String authorization = null;
		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for (Cookie cookie : cookies) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 로그인 유저
				if (cookie.getName().equals("Authorization")) {
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
				// price를 키로 사용하고, 에러 코드를 값으로 사용하여, 반환용 Map에 추가한다.
				productMap.put("price", "0");
				// 에러 코드가 추가된 Map을 반환한다.
				return productMap;
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// price를 키로 사용하고, 에러 코드를 값으로 사용하여, 반환용 Map에 추가한다.
				productMap.put("price", "-4");
				// 에러 코드가 추가된 Map을 반환한다.
				return productMap;
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// price를 키로 사용하고, 에러 코드를 값으로 사용하여, 반환용 Map에 추가한다.
			productMap.put("price", "-99");
			// 에러 코드가 추가된 Map을 반환한다.
			return productMap;
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// price를 키로 사용하고, 에러 코드를 값으로 사용하여, 반환용 Map에 추가한다.
				productMap.put("price", "-1");
				// 에러 코드가 추가된 Map을 반환한다.
				return productMap;
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// price를 키로 사용하고, 에러 코드를 값으로 사용하여, 반환용 Map에 추가한다.
					productMap.put("price", "-100");
					// 에러 코드가 추가된 Map을 반환한다.
					return productMap;
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

		// 토큰에서 추출한 로그인 유저 idx와 도토리에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != idx ) {
			// price를 키로 사용하고, 에러 코드를 값으로 사용하여, 반환용 Map에 추가한다.
			productMap.put("price", "-4");
			// 에러 코드가 추가된 Map을 반환한다.
			return productMap;
		}

		// 상품 번호에 해당하는 상품이 존재하는지 체크한다.
		Product product = productService.findByProductIdx(productIdx);

		// 상품이 존재하지 않는 경우
		if ( product == null ) {
			// price를 키로 사용하고, 에러 코드를 값으로 사용하여, 반환용 Map에 추가한다.
			productMap.put("price", "-9");

			// 에러 코드가 추가된 Map을 반환한다.
			return productMap;
		}

		// 상품이 존재하는 경우

		// name을 키로 사용하고, 상품 이름을 값으로 사용하여, 반환용 Map에 추가한다.
		productMap.put("name", product.getName());
		// price를 키로 사용하고, 상품 가격을 값으로 사용하여, 반환용 Map에 추가한다.
		productMap.put("price", product.getPrice());
		// idx를 키로 사용하고, 로그인 유저 idx를 값으로 사용하여, 반환용 Map에 추가한다.
		productMap.put("idx", loginIdx);

		// 상품 정보가 추가된 Map을 반환한다.
		return productMap;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////상품 - 도토리
	// 도토리 팝업
	@RequestMapping("/dotory/{idx}") // 경로 매개변수
	public String dotory(@PathVariable int idx,
						 @RequestParam(required = false) String success,
						 @RequestParam(required = false) String error,
						 Model model) {
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
				// 도토리 팝업으로 이동
				return "Page/dotory";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 도토리 팝업으로 이동
				return "Page/dotory";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 도토리 팝업으로 이동
			return "Page/dotory";
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
				// 도토리 팝업으로 이동
				return "Page/dotory";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 도토리 팝업으로 이동
					return "Page/dotory";
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
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 도토리에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != idx ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 도토리 팝업으로 이동
			return "Page/dotory";
		}

		// 로그인 유저 idx에 해당하는 유저 메인 정보를 조회한다.
		UserMain userMain = signService.findUserMainBySignIdx(loginIdx);
		// 유저 정보 DTO를 생성한다.
		UserDTO userDTO = new UserDTO();
		// 조회한 유저 메인 정보와 로그인 유저 idx를 전달하여 유저 정보 DTO로 변환한다.
		userDTO.toDotoryPopupDTO(userMain, loginIdx);
		// 변환한 유저 정보 DTO를 바인딩한다.
		model.addAttribute("sign", userDTO);

		// 상품 타입이 도토리에 해당하는 상품 정보를 모두 조회하여 리스트로 가져온다.
		List<Product> productList = productService.findByProductType(1);
		// 가져온 도토리 상품 리스트를 바인딩한다.
		model.addAttribute("productList", productList);

		// UUID와 결제 시간을 합쳐서 상품 주문 번호로 만들어 바인딩한다.
		model.addAttribute("orderId", UUID.randomUUID() + ":" + System.currentTimeMillis());
		// Pay Client ID를 바인딩한다.
		model.addAttribute("clientId", payClientId);

		// 파라미터로 받아온 결제 메시지가 존재하는지 체크한다.
		// 파라미터로 받아온 결제 성공 메시지가 존재하는 경우
		if ( success!= null ) {
			// 결제 성공 메시지를 바인딩한다.
			model.addAttribute("msg", success);
		}
		// 파라미터로 받아온 결제 실패 메시지가 존재하는 경우
		if ( error != null ) {
			// 결제 실패 메시지를 바인딩한다.
			model.addAttribute("msg", error);
		}

		// 도토리 팝업으로 이동
		return "Page/dotory";
	}

	// 도토리 결제
	@RequestMapping("/dotory/pay_dotory")
	public String dotoryBuy(int idx, String tid, int amount, Model model) throws JsonProcessingException, UnsupportedEncodingException {
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
				// 도토리 팝업으로 이동
				return "Page/dotory";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 도토리 팝업으로 이동
				return "Page/dotory";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 도토리 팝업으로 이동
			return "Page/dotory";
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
				// 도토리 팝업으로 이동
				return "Page/dotory";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 도토리 팝업으로 이동
					return "Page/dotory";
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
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 도토리에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != idx ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 도토리 팝업으로 이동
			return "Page/dotory";
		}

		// 요청된 결제 정보를 가지고 결제 서버로 이동해서 최종 결제를 승인 받는다.
		JsonNode jsonResponse = NicePay.getResponse(payClientId, payClientSecret, tid, amount);
		// 최종 결제 승인이 끝나고 반환받은 정보 중 승인 코드를 가지고 결제에 성공했는지 실패했는지 체크한다.
		String resultCode = jsonResponse.get("resultCode").asText();

		// 결제에 성공한 경우
		if ( resultCode.equals("0000") ) {
			// 로그인 유저 idx에 해당하는 유저 메인 정보를 조회한다.
			UserMain userMain = signService.findUserMainBySignIdx(loginIdx);
			// 조회한 유저 메인 정보 중 도토리 개수에 결제한 도토리 개수를 더해서 setter를 통해 전달한다.
			userMain.setDotory(userMain.getDotory()+amount);
			// 전달받은 도토리 개수로 갱신한다.
			signService.updateSetDotoryByIdx(userMain);

			// 결제 내역 Entity를 생성한다.
			PayProduct payProduct = new PayProduct();
			// 상품 주문 번호에 반환받은 결제 정보 중 상품 주문 번호를 가져와 setter를 통해 전달한다.
			payProduct.setOrderId(jsonResponse.get("orderId").asText());
			// 결제 내역 idx에 로그인 유저 idx를 setter를 통해 전달한다.
			payProduct.setIdx(loginIdx);
			// 상품 이름에 반환받은 결제 정보 중 상품 이름을 가져와 setter를 통해 전달한다.
			payProduct.setName(jsonResponse.get("goodsName").asText());
			// 전달받은 결제 내역을 저장한다.
			productService.insertIntoPayProduct(payProduct);

			// 로그인 유저 idx와 결제 완료 메시지를 들고 도토리 걸제 페이지 URL로 이동
			return "redirect:/dotory/" + loginIdx + "?success=" + URLEncoder.encode("결제가 완료되었습니다.", "UTF-8");
		}

		// 결제에 실패한 경우

		// 로그인 유저 idx와 결제 실패 메시지를 들고 도토리 결제 페이지 URL로 이동
		return "redirect:/dotory/" + loginIdx + "?error=" + URLEncoder.encode("결제에 실패하였습니다.", "UTF-8");
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////일촌
	@RequestMapping("/main_ilchon")
	@ResponseBody
	public String main_follow(Ilchon ilchon) {
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
				// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
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

		// 토큰에서 추출한 로그인 유저 idx와 일촌에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != ilchon.getIlchonSessionIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 일촌 맺기 시작 //

		/* 먼저 로그인 유저가 해당 미니홈피 유저에게 일촌 신청했는지 확인
		 * 그 다음 반대로 해당 미니홈피 유저가 로그인 유저에게 일촌 신청했는지 확인
		 * count함수를 사용하여 값을 숫자로 받는다
		 * 0: 로그인 유저 및 해당 미니홈피 유저 둘 다 일촌 신청 안 한 상태
		 * 1: 로그인 유저 및 해당 미니홈피 유저 둘 중 하나는 일촌 신청한 상태
		 * 2: 로그인 유저 및 해당 미니홈피 유저 둘 다 일촌 신청한 상태
		 */
		int followNum = mainService.countByIlchonIdxAndIlchonSessionIdxOrIlchonIdxAndIlchonSessionIdx(ilchon);

		// 일촌 신청 결과를 전달할 변수
		String result = "";

		/* Ilchon - ilchonUp : 로그인 유저와 해당 미니홈피 유저와의 일촌 관계
		 * 0: 둘 다 일촌 신청 안 한 상태
		 * 1: 둘 중 하나는 일촌 신청한 상태
		 * 2: 둘 다 일촌 신청한 상태
		 */

		// 로그인 유저 및 해당 미니홈피 유저 둘다 일촌 신청 안 한 경우
		// 이때는 다른 것을 더 조회할 필요없이 바로 일촌 신청 상태로 만든다.
		if ( followNum == 0 ) {
			// 미니홈피 유저 idx에 해당하는 유저 프로필 정보를 조회한다.
			UserProfile miniUser = signService.findUserProfileBySignIdx(ilchon.getIlchonIdx());

			// 일촌 idx에 AUTO_INCREMENT로 null을 설정하여 setter를 통해 전달한다.
			ilchon.setIdx(null);
			// 로그인 유저만 일방적으로 일촌 신청을 하였기에 일촌 상태에 일촌을 의미하는 1을 setter를 통해 전달한다.
			ilchon.setIlchonUp(1);
			// 일촌 신청자에 신청자 이름을 만들어 setter를 통해 전달한다.
			/* 이메일 @부분까지 잘라낸뒤 플랫폼명 추가 - 폐기
			 * 네이버 - qwer@ + naver = qwer@naver
			 * 카카오 - qwer@ + kakao = qwer@kakao
			 */
			// ilchonName = (miniUser.getEmail().substring( 0, miniUser.getEmail().indexOf("@") + 1 ) + miniUser.getPlatform());

			/* 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
			 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 */
			ilchon.setIlchonName( "( " + miniUser.getName() + " / " + miniUser.getEmail().substring( 0, miniUser.getEmail().indexOf("@") ) + " )" );
			// 전달받은 일촌 신청 정보를 저장한다.
			mainService.insertIntoIlchon(ilchon);

			// 일촌 신청된 경우
			result = "yes";
		}
		// 로그인 유저 및 해당 미니홈피 유저 둘 중 하나는 일촌 신청한 경우
		// 이때는 로그인 유저가 일촌 신청을 했는지, 해당 미니홈피 유저가 일촌 신청을 했는지 모르기에 한 번 더 조회한다.
		if ( followNum == 1 ) {
			// 로그인 유저가 미니홈피 유저에게 일촌 신청을 했는지 조회한다.
			Ilchon ilchonUser = mainService.findByIlchonIdxAndIlchonSessionIdx(ilchon);

			// 로그인 유저가 해당 미니홈피 유저에게 일촌 신청 X
			// 해당 미니홈피 유저가 로그인 유저에게 일촌 신청 O
			if ( ilchonUser == null ) {
				// 미니홈피 유저 idx에 해당하는 유저 프로필 정보를 조회한다.
				UserProfile miniUser = signService.findUserProfileBySignIdx(ilchon.getIlchonIdx());

				// 일촌 idx에 AUTO_INCREMENT로 null을 설정하여 setter를 통해 전달한다.
				ilchon.setIdx(null);
				// 로그인 유저가 마저 일촌 상태가 되면서 이제 맞일촌 상태가 됐으므로 일촌 상태에 맞일촌을 의미하는 2를 setter를 통해 전달한다.
				ilchon.setIlchonUp(2);
				// 일촌 신청자에 신청자 이름을 만들어 setter를 통해 전달한다.
				/* 이메일 @부분까지 잘라낸 뒤 플랫폼명 추가 - 폐기
				 * 네이버 - qwer@ + naver = qwer@naver
				 * 카카오 - qwer@ + kakao = qwer@kakao
				 */
				// ilchonName = (miniUser.getEmail().substring( 0, miniUser.getEmail().indexOf("@") + 1 ) + miniUser.getPlatform());

				/* 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
				 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
				 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
				 */
				ilchon.setIlchonName( "( " + miniUser.getName() + " / " + miniUser.getEmail().substring( 0, miniUser.getEmail().indexOf("@") ) + " )" );
				// 전달받은 일촌 신청 정보를 저장한다.
				mainService.insertIntoIlchon(ilchon);
				// 미니홈피 유저의 일촌 상태를 2로 갱신한다.
				mainService.updateSetIlchonUpByIlchonIdxAndIlchonSessionIdx(ilchon);

				// 일촌 신청된 경우
				result = "yes";
			// 해당 미니홈피 유저가 로그인 유저에게 일촌 신청 X
			// 로그인 유저가 해당 미니홈피 유저에게 일촌 신청 O
			} else {
				// 이미 일촌 신청된 것을 다시 눌렀기에 일촌 해제 상태로 만든다.
				// 저장되어 있던 일촌 신청 정보를 삭제한다.
				mainService.deleteByIlchonIdxAndIlchonSessionIdx(ilchon);

				// 일촌 해제된 경우
				result = "no";
			}
		}
		// 로그인 유저 및 해당 미니홈피 유저 둘 다 일촌 신청한 경우
		// 이때는 다른 것을 더 조회할 필요없이 바로 일촌 해제 상태로 만든다.
		if ( followNum == 2 ) {
			// 저장되어 있던 일촌 신청 정보를 삭제한다.
			mainService.deleteByIlchonIdxAndIlchonSessionIdx(ilchon);
			// 로그인 유저가 일촌 해제 상태가 되면서 이제 맞일촌 상태가 아니므로 일촌 상태에 일촌을 의미하는 1을 setter를 통해 전달한다.
			ilchon.setIlchonUp(1);
			// 미니홈피 유저의 일촌 상태를 1로 갱신한다.
			mainService.updateSetIlchonUpByIlchonIdxAndIlchonSessionIdx(ilchon);

			// 일촌 해제된 경우
			result = "no";
		}

		// 일촌 맺기 끝 //

		// 맞일촌 구하기 //

		// 맞일촌 유저를 찾기 위해 일촌 상태를 맞일촌을 의미하는 2를 setter를 통해 전달한다.
		ilchon.setIlchonUp(2);
		// 미니홈피 유저와 맞일촌인 유저들의 수를 COUNT로 조회한다.
		int ilchonNum = mainService.countByIlchonIdxAndIlchonUp(ilchon);
		// 미니홈피 유저 idx에 해당하는 유저 메인 정보 중 일촌 수를 조회한 맞일촌 수로 갱신한다.
		signService.updateSetIlchonByIdx(ilchonNum, ilchon.getIlchonIdx());

		// 로그인 유저의 맞일촌 수를 조회하기 위해 일촌 idx에 로그인 유저 idx를 setter를 통해 전달한다.
		ilchon.setIlchonIdx(loginIdx);
		// 로그인 유저와 맞일촌인 유저들의 수를 COUNT로 조회한다.
		int ilchonReverseNum = mainService.countByIlchonIdxAndIlchonUp(ilchon);
		// 로그인 유저 idx에 해당하는 유저 메인 정보 중 일촌 수를 조회한 맞일촌 수로 갱신한다.
		signService.updateSetIlchonByIdx(ilchonReverseNum, loginIdx);

		// 맞일촌 끝 //

		// 결과 메시지 전달
		return result;
	}
}