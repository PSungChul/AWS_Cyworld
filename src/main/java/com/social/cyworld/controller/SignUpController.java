package com.social.cyworld.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.social.cyworld.dto.JoinDTO;
import com.social.cyworld.httpclient.IamPortPass;
import com.social.cyworld.util.JwtUtil;
import com.social.cyworld.entity.Sign;
import com.social.cyworld.util.MailKey;
import com.social.cyworld.service.SignService;
import com.social.cyworld.util.PhoneKey;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@PropertySource("classpath:application-information.properties")
@Controller
public class SignUpController {
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

	// properties - SMTP ID/PWD
	@Value("${naverId:naverId}")
	private String naverId;
	@Value("${naverPwd:naverPwd}")
	private String naverPwd;
	// properties - IamPortPass
	@Value("${impNumber:impNumber}")
	private String impNumber;
	@Value("${impKey:impKey}")
	private String impKey;
	@Value("${impSecret:impSecret}")
	private String impSecret;
	// properties - naver
	@Value("${naverClientId:naverClientId}")
	private String naverClientId;
	// properties - kakao
	@Value("${kakaoKey:kakaoKey}")
	private String kakaoKey;
	// properties - sens
	@Value("${naverAccessKey:naverAccessKey}")
	private String naverAccessKey;
	@Value("${naverSecretKey:naverSecretKey}")
	private String naverSecretKey;
	@Value("${naverSensKey:naverSensKey}")
	private String naverSensKey;
	@Value("${smsPhoneNumber:smsPhoneNumber}")
	private String smsPhoneNumber;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 기본 및 로그인
	@RequestMapping(value= {"/", "login"})
	public String basic(Model model) {
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
		// 쿠키에 토큰이 존재하지 않는 경우 - 로그인
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 로그아웃 페이지로 이동
				return "redirect:/logout";
			// 토큰도 세션도 존재하지 않는 경우 - 로그인
			} else {
				model.addAttribute("naverClientId", naverClientId);
				model.addAttribute("kakaoKey", kakaoKey);

				// 로그인 페이지로 이동
				return "Sign/login";
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
				// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
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

		// 토큰과 세션이 모두 존재한다면 로그인은 건너뛰고 토큰에서 추출한 로그인 유저 idx에 해당하는 메인 페이지로 이동
		return "redirect:/main/" + loginIdx;
	}
	
	// 로그아웃
	@RequestMapping("/logout")
	public String logout() {
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
		// 쿠키에 토큰이 존재하지 않는 경우 - 세션만 삭제
		if ( authorization == null ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하는 경우
			if (session.getAttribute("login") != null) {
				// 세션을 삭제한다.
				session.removeAttribute("login");
			}

			// 세션이 삭제되면 다시 로그인 페이지로 이동
			return "redirect:/login";
		}
		// 쿠키에 토큰이 존재하는 경우 - Redis 블랙리스트에 토큰을 추가하고 토큰과 세션 모두 삭제
		// 무효화한 토큰인지 체크한다.
		int logoutIdx = jwtUtil.validationToken(authorization);
		// 무효화한 토큰이 아닌 경우 - 무효화한 토큰은 이미 블랙리스트에 등록돼있으니 더 추가할 필요가 없다.
		if ( logoutIdx != -99 ) {
			// Redis 블랙리스트에 토큰을 추가한다.
			jwtUtil.logoutToken(authorization);
		}

		// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
		Cookie deleteCookie = new Cookie("Authorization", "");
		// Authorization 쿠키의 만료 시간을 0으로 설정한다.
		deleteCookie.setMaxAge(0);
		// 삭제할 Authorization 쿠키를 추가한다.
		response.addCookie(deleteCookie);

		// 세션이 존재하는지 체크한다.
		HttpSession session = request.getSession();
		// 세션이 존재하는 경우
		if ( session.getAttribute("login") != null ) {
			// 세션을 삭제한다.
			session.removeAttribute("login");
		}

		// 토큰과 세션이 모두 삭제되면 다시 로그인 페이지로 이동
		return "redirect:/login";
	}
	
	// 네이버 API 콜백용
	@RequestMapping("/login_naver_callback")
	public String naver_join(Model model) {
		model.addAttribute("naverClientId", naverClientId);
		// 네이버 콜백 페이지로 이동
		return "Sign/login_naver_callback";
	}
	
	// 싸이월드 가입자와 비가입자 구별
	@RequestMapping("/login_authentication")
	public String login_authentication(Sign sign, Model model) {
		// 파라미터로 받아온 유저정보 중 이메일이 없는 경우 - 회원가입
		if ( sign.getEmail() == null ) {
			// 파라미터로 받아온 유저정보를 바인딩한다.
			model.addAttribute("sign", sign);
			// IamPort 가맹점 번호를 바인딩한다.
			model.addAttribute("impNumber", impNumber);

			// cyworld 회원가입 페이지로 이동
			return "Sign/cyworld_join";
		}

		// 파라미터로 받아온 유저정보 중 이메일이 있는 경우 - 로그인

		// 로그인한 이메일로 유저정보를 조회한다.
		Sign login = signService.findByEmail(sign.getEmail());

		// Authorization 쿠키에 토큰이 존재하는지 체크한다.
		Cookie[] cookies = request.getCookies();
		// Authorization 쿠키가 존재하는 경우
		if ( cookies != null ) {
			// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
			for ( Cookie cookie : cookies ) {
				// Authorization 쿠키에 토큰이 존재하는 경우 - 만료 or 무효화 이후 로그인
				if ( cookie.getName().equals("Authorization") ) {
					// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCookie = new Cookie("Authorization", "");
					// Authorization 쿠키의 만료 시간을 0으로 설정한다.
					deleteCookie.setMaxAge(0);
					// 삭제할 Authorization 쿠키를 추가한다.
					response.addCookie(deleteCookie);

					// JWT에서 로그인 유저 idx로 토큰을 생성한다.
					String loginToken = jwtUtil.createToken(login.getIdx());

					// Authorization 쿠키에 생성한 토큰을 부여한다.
					Cookie tokenCookie = new Cookie("Authorization", loginToken);
					// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
					int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(loginToken);
					// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
					tokenCookie.setMaxAge(refreshExpiryDate);
					// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
					tokenCookie.setHttpOnly(true);
					// 생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
					response.addCookie(tokenCookie);

					// 로그인용 세션을 생성한다.
					HttpSession session = request.getSession();
					// 로그인용 세션으로 "login"을 지정한다.
					session.setAttribute("login", "login");

					// 접속 날짜를 기록하기 위해 Date객체를 생성한다.
					Date date = new Date();
					// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다.
					SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
					// 위에서 구한 현재 날짜를 로그인한 유저의 접속 날짜에 입력한다.
					login.setToDate(today.format(date));
					// 로그인한 유저의 접속 날짜를 갱신한다.
					signService.updateTodayDate(login);

					// 로그인 유저 idx에 해당하는 메인 페이지로 이동
					return "redirect:/main/" + login.getIdx();
				}
			}
		}
		// 쿠키에 토큰이 존재하지 않는 경우 - 로그이웃 이후 로그인
		// JWT에서 로그인 유저 idx로 토큰을 생성한다.
		String loginToken = jwtUtil.createToken(login.getIdx());

		// Authorization 쿠키에 생성한 토큰을 부여한다.
		Cookie tokenCookie = new Cookie("Authorization", loginToken);
		// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
		int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(loginToken);
		// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
		tokenCookie.setMaxAge(refreshExpiryDate);
		// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
		tokenCookie.setHttpOnly(true);
		// 생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
		response.addCookie(tokenCookie);

		// 로그인용 세션을 생성한다.
		HttpSession session = request.getSession();
		// 로그인용 세션으로 "login"을 지정한다.
		session.setAttribute("login", "login");

		// 접속 날짜를 기록하기 위해 Date객체를 생성한다.
		Date date = new Date();
		// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다.
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
		// 위에서 구한 현재 날짜를 로그인한 유저의 접속 날짜에 입력한다.
		login.setToDate(today.format(date));
		// 로그인한 유저의 접속 날짜를 갱신한다.
		signService.updateTodayDate(login);

		// 로그인 유저 idx에 해당하는 메인 페이지로 이동
		return "redirect:/main/" + login.getIdx();
	}

	// 카카오 가입자 비가입자 구별
	@RequestMapping("/kakao_authentication")
	@ResponseBody
	public int kakao_authentication(String email, Model model) {
		// 가입자가 아닌 경우 - 회원가입 코드
		int result = 0;

		// 이메일에 해당하는 가입 유저정보가 존재하는지 조회하여 체크한다.
		Sign join = signService.findByEmail(email);
		// 조회한 유저정보가 존재하지 않는 경우 - 비가입자
		if ( join == null ) {
			// 회원가입 코드 전달
			return result;
		// 조회한 유저정보가 존재하는 경우 - 가입자
		} else {
			// 조회한 유저정보 중 플랫폼이 카카오인 경우
			if ( join.getPlatform().equals("kakao") ) {
				// 로그인 유저 idx로 변경
				result = join.getIdx();

				// Authorization 쿠키에 토큰이 존재하는지 체크한다.
				Cookie[] cookies = request.getCookies();
				// Authorization 쿠키가 존재하는 경우
				if ( cookies != null ) {
					// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
					for ( Cookie cookie : cookies ) {
						// Authorization 쿠키에 토큰이 존재하는 경우 - 만료 or 무효화 이후 로그인
						if ( cookie.getName().equals("Authorization") ) {
							// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
							Cookie deleteCookie = new Cookie("Authorization", "");
							// Authorization 쿠키의 만료 시간을 0으로 설정한다.
							deleteCookie.setMaxAge(0);
							// 삭제할 Authorization 쿠키를 추가한다.
							response.addCookie(deleteCookie);

							// JWT에서 로그인 유저 idx로 토큰을 생성한다.
							String loginToken = jwtUtil.createToken(join.getIdx());

							// Authorization 쿠키에 생성한 토큰을 부여한다.
							Cookie tokenCookie = new Cookie("Authorization", loginToken);
							// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
							int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(loginToken);
							// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
							tokenCookie.setMaxAge(refreshExpiryDate);
							// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
							tokenCookie.setHttpOnly(true);
							// 생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
							response.addCookie(tokenCookie);

							// 로그인용 세션을 생성한다.
							HttpSession session = request.getSession();
							// 로그인용 세션으로 "login"을 지정한다.
							session.setAttribute("login", "login");

							// 접속 날짜를 기록하기 위해 Date객체를 생성한다.
							Date date = new Date();
							// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다.
							SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
							// 위에서 구한 현재 날짜를 로그인한 유저의 접속 날짜에 입력한다.
							join.setToDate(today.format(date));
							// 로그인한 유저의 접속 날짜를 갱신한다.
							signService.updateTodayDate(join);

							// 로그인 유저 idx 전달
							return result;
						}
					}
				}
				// 쿠키에 토큰이 존재하지 않는 경우 - 로그이웃 이후 로그인
				// JWT에서 로그인 유저 idx로 토큰을 생성한다.
				String loginToken = jwtUtil.createToken(join.getIdx());

				// Authorization 쿠키에 생성한 토큰을 부여한다.
				Cookie tokenCookie = new Cookie("Authorization", loginToken);
				// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
				int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(loginToken);
				// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
				tokenCookie.setMaxAge(refreshExpiryDate);
				// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
				tokenCookie.setHttpOnly(true);
				// 생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
				response.addCookie(tokenCookie);

				// 로그인용 세션을 생성한다.
				HttpSession session = request.getSession();
				// 로그인용 세션으로 "login"을 지정한다.
				session.setAttribute("login", "login");

				// 접속 날짜를 기록하기 위해 Date객체를 생성한다.
				Date date = new Date();
				// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다.
				SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
				// 위에서 구한 현재 날짜를 로그인한 유저의 접속 날짜에 입력한다.
				join.setToDate(today.format(date));
				// 로그인한 유저의 접속 날짜를 갱신한다.
				signService.updateTodayDate(join);

				// 로그인 유저 idx 전달
				return result;
			// 조회한 유저정보 중 플랫폼이 카카오가 아닌 경우
			} else {
				// 조죄한 유저정보 중 플랫픔이 싸이월드인 경우
				if ( join.getPlatform().equals("cyworld") ) {
					// 에러 코드로 변경
					result = -1;
					// 조죄한 유저정보 중 플랫픔이 네이버인 경우
				} else if ( join.getPlatform().equals("naver") ) {
					// 에러 코드로 변경
					result = -2;
				}

				// 에러 코드 전달
				return result;
			}
		}
	}

	// 네이버 가입자 비가입자 구별
	@RequestMapping("/naver_authentication")
	@ResponseBody
	public int naver_authentication(Sign sign, Model model) {
		// 가입자가 아닌 경우 - 회원가입 코드
		int result = 0;
		// 휴대폰 번호 하이픈 제거
		sign.setPhoneNumber(sign.getPhoneNumber().replaceAll("-", ""));

		// 이름 + 생년월일 + 휴대폰 번호에 해당하는 가입 유저정보가 존재하는지 조회하여 체크한다.
		Sign join = signService.findByNameAndBirthdayAndPhoneNumber(sign.getName(), sign.getBirthday(), sign.getPhoneNumber());
		// 조회한 유저정보가 존재하지 않는 경우 - 비가입자
		if ( join == null ) {
			// 회원가입 코드 전달
			return result;
		// 조회한 유저정보가 존재하는 경우 - 가입자
		} else {
			// 조회한 유저정보 중 플랫폼이 네이버인 경우
			if ( join.getPlatform().equals("naver") ) {
				// 로그인 유저 idx로 변경
				result = join.getIdx();

				// Authorization 쿠키에 토큰이 존재하는지 체크한다.
				Cookie[] cookies = request.getCookies();
				// Authorization 쿠키가 존재하는 경우
				if ( cookies != null ) {
					// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
					for ( Cookie cookie : cookies ) {
						// Authorization 쿠키에 토큰이 존재하는 경우 - 만료 or 무효화 이후 로그인
						if ( cookie.getName().equals("Authorization") ) {
							// Authorization 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
							Cookie deleteCookie = new Cookie("Authorization", "");
							// Authorization 쿠키의 만료 시간을 0으로 설정한다.
							deleteCookie.setMaxAge(0);
							// 삭제할 Authorization 쿠키를 추가한다.
							response.addCookie(deleteCookie);

							// JWT에서 로그인 유저 idx로 토큰을 생성한다.
							String loginToken = jwtUtil.createToken(join.getIdx());

							// Authorization 쿠키에 생성한 토큰을 부여한다.
							Cookie tokenCookie = new Cookie("Authorization", loginToken);
							// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
							int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(loginToken);
							// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
							tokenCookie.setMaxAge(refreshExpiryDate);
							// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
							tokenCookie.setHttpOnly(true);
							// 생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
							response.addCookie(tokenCookie);

							// 로그인용 세션을 생성한다.
							HttpSession session = request.getSession();
							// 로그인용 세션으로 "login"을 지정한다.
							session.setAttribute("login", "login");

							// 접속 날짜를 기록하기 위해 Date객체를 생성한다.
							Date date = new Date();
							// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다.
							SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
							// 위에서 구한 현재 날짜를 로그인한 유저의 접속 날짜에 입력한다.
							join.setToDate(today.format(date));
							// 로그인한 유저의 접속 날짜를 갱신한다.
							signService.updateTodayDate(join);

							// 로그인 유저 idx 전달
							return result;
						}
					}
				}
				// 쿠키에 토큰이 존재하지 않는 경우 - 로그이웃 이후 로그인
				// JWT에서 로그인 유저 idx로 토큰을 생성한다.
				String loginToken = jwtUtil.createToken(join.getIdx());

				// Authorization 쿠키에 생성한 토큰을 부여한다.
				Cookie tokenCookie = new Cookie("Authorization", loginToken);
				// 리프레쉬 토큰의 만료까지 남은 시간을 구한다.
				int refreshExpiryDate = jwtUtil.refreshTokenExpiryDate(loginToken);
				// Authorization 쿠키의 만료 시간을 리프레쉬 토큰의 만료까지 남은 시간으로 설정한다.
				tokenCookie.setMaxAge(refreshExpiryDate);
				// Authorization 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
				tokenCookie.setHttpOnly(true);
				// 생성한 토큰이 부여된 Authorization 쿠키를 추가한다.
				response.addCookie(tokenCookie);

				// 로그인용 세션을 생성한다.
				HttpSession session = request.getSession();
				// 로그인용 세션으로 "login"을 지정한다.
				session.setAttribute("login", "login");

				// 접속 날짜를 기록하기 위해 Date객체를 생성한다.
				Date date = new Date();
				// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다.
				SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
				// 위에서 구한 현재 날짜를 로그인한 유저의 접속 날짜에 입력한다.
				join.setToDate(today.format(date));
				// 로그인한 유저의 접속 날짜를 갱신한다.
				signService.updateTodayDate(join);

				// 로그인 유저 idx 전달
				return result;
			// 조회한 유저정보 중 플랫폼이 네이버가 아닌 경우
			} else {
				// 조회한 유저정보 중 플랫폼이 싸이월드인 경우
				if ( join.getPlatform().equals("cyworld") ) {
					// 에러 코드로 변경
					result = -1;
					// 조회한 유저정보 중 플랫폼이 카카오인 경우
				} else if ( join.getPlatform().equals("kakao") ) {
					// 에러 코드로 변경
					result = -2;
				}

				// 에러 코드 전달
				return result;
			}
		}
	}

	// 카카오 가입 페이지로 이동
	@RequestMapping("/kakao_join_form")
	public String kakaoJoinForm(Sign sign, Model model) {
		// 파라미터로 받아온 유저정보를 바인딩한다.
		model.addAttribute("sign", sign);
		// IamPort 가맹점 번호를 바인딩한다.
		model.addAttribute("impNumber", impNumber);

		// 카카오 회원가입 페이지
		return "Sign/kakao_join";
	}

	// 네이버 가입 페이지로 이동
	@RequestMapping("/naver_join_form")
	public String naverJoinForm(Sign sign, Model model) {
		// 파라미터로 받아온 유저정보를 바인딩한다.
		model.addAttribute("sign", sign);

		// 네이버 회원가입 페이지
		return "Sign/naver_join";
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////이메일 인증 및 본인인증
	// 이메일 인증
	@RequestMapping("/email_send")
	@ResponseBody
	public HashMap<String, String> emailSend(String email) {
		// 이메일 전송하기 전 중복된 이메일인지 조회
		Sign sign = signService.findByEmail(email);

		// 중복된 이메일인 경우
		if ( sign != null ) {
			HashMap<String, String> map = new HashMap<>();
			map.put("email", "0");
			map.put("msg", "이미 가입된 이메일입니다.\n로그인 혹은 아이디/비밀번호 찾기를 이용해주세요");
			return map;
		}

		// 미리 만들어 둔 랜덤키 생성 메소드를 mail패키지의 MailKey.java에서 가져와 사용한다
		String emailKey = new MailKey().getKey(10, false); // 랜덤키 길이 설정
		// Mail Server 설정
		String charSet = "UTF-8"; // 사용할 언어셋
		String hostSMTP = "smtp.naver.com"; // 사용할 SMTP
		String hostSMTPid = naverId; // 사용할 SMTP에 해당하는 ID - 이메일 형식
		String hostSMTPpwd = naverPwd; // 사용할 ID에 해당하는 PWD
		
		// 가장 중요한 TLS설정 - 이것이 없으면 신뢰성 에러가 나온다
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		
		// 보내는 사람 E-Mail, 제목, 내용 
		String fromEmail = naverId; // 보내는 사람 email - - hostSMTPid와 동일하게 작성
		String fromName = "관리자"; // 보내는 사람 이름
		String subject = "[Cyworld] 이메일 인증번호 발송 안내입니다."; // 제목
		
		// 받는 사람 E-Mail 주소
		String mail = email; // 받는 사람 email
		
		try {
			HtmlEmail htmlEmail = new HtmlEmail(); // Email 생성
			htmlEmail.setDebug(true);
			htmlEmail.setCharset(charSet); // 언어셋 사용
			htmlEmail.setSSL(true);
			htmlEmail.setHostName(hostSMTP); // SMTP 사용
			htmlEmail.setSmtpPort(587);	// SMTP 포트 번호 입력

			htmlEmail.setAuthentication(hostSMTPid, hostSMTPpwd); // 메일 ID, PWD
			htmlEmail.setTLS(true);
			htmlEmail.addTo(mail); // 받는 사람
			htmlEmail.setFrom(fromEmail, fromName, charSet); // 보내는 사람
			htmlEmail.setSubject(subject); // 제목
			htmlEmail.setHtmlMsg(
					"<p>" + "[메일 인증 안내입니다.]" + "</p>" +
					"<p>" + "Cyworld를 사용해 주셔서 감사드립니다." + "</p>" +
					"<p>" + "아래 인증 코드를 '인증번호'란에 입력해 주세요." + "</p>" +
					"<p>" + "[" + emailKey + "]" + "</p>"); // 본문 내용
			htmlEmail.send(); // 메일 보내기

			// 전송한 이메일을 암호화한다.
			String passwordEmail = passwordEncoder.encode(email);
			// 전송한 이메일 인증번호를 암호화한다.
			String passwordEmailKey = passwordEncoder.encode(emailKey);

			// EmailKey 쿠키에 암호화된 이메일과 암호환 이메일 인증번호을 합쳐서 부여한다.
			Cookie emailCookie = new Cookie("Email", passwordEmail + ":" + passwordEmailKey);
			// EmailKey 쿠키의 만료 시간을 5분으로 설정한다.
			emailCookie.setMaxAge(300);
			// EmailKey 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
			emailCookie.setHttpOnly(true);
			// 암호화된 이메일과 암호환 이메일 인증번호가 합쳐서 부여된 Email 쿠키를 추가한다.
			response.addCookie(emailCookie);

			// 반환용 Map을 생성한다.
			HashMap<String, String> map = new HashMap<>();
			// email을 키로 사용하고, 전송한 이메일을 값으로 사용하여 반환용 Map에 추가한다.
			map.put("email", email);
			// msg를 키로 사용하고, 암호화된 이메일 인증번호를 값으로 사용하여 반환용 Map에 추가한다.
			map.put("msg", passwordEmailKey);

			// 이메일 인증정보가 추가된 반환용 Map을 반환한다.
			return map;
		} catch (Exception e) {
			System.out.println(e);
			// 메일 보내기가 실패하면 "false"를 콜백 메소드에 전달
			HashMap<String, String> map = new HashMap<>();
			map.put("email", "-1");
			map.put("msg", "메일 발송에 실패하였습니다.\n다시 시도해주시기 바랍니다.");
			return map;
		}
	}
	// 이메일 인증 체크
	@RequestMapping("/email_check")
	@ResponseBody
	public boolean emailCheck(String emailKey, String hEmailKey) {
		boolean isMatch = passwordEncoder.matches(emailKey, hEmailKey);
		// 반환된 결과 값(true/false)을 클라이언트로 반환한다.
		// true - 이메일 인증 번호 일치 / false - 이메일 인증 번호 불일치
		return isMatch;
	}

	// 휴대폰 본인인증 IamPort서버 통신
	@PostMapping("/certification")
	@ResponseBody
	public HashMap<String, String> certifications(String impUid) { // 파라미터로 fetch를 통해 넘어온 imp_uid를 받아온다.
		// IamPort로부터 access_token을 받아와야 하기에 IamPort 서버와 통신하는 메소드를 실행한다.
		JsonNode jsonToken = IamPortPass.getToken(impKey, impSecret);
		// IamPort로부터 반환받은 값에서 필요한 access_token을 가져와 토큰 변수에 전달한다.
		// toString - 객체나 값을 문자열로 변환하는 메소드로, 디버깅이나 출력용으로 사용한다. - "" 큰 따옴표가 항시 따라 붙는다.
		// asText - 일반적으로 사람이 읽을 수 있는 형식으로 값을 문자열로 변환하는 데 사용한다. - "" 큰 따옴표 없이 글자만 나온다.
		// 여기서 두가지의 눈에 띄는 차이는 "" 큰 따옴표의 유무인데, 값을 URL에 담아서 함께 보내는 GET 방식에는 데이터에 따라 붙는 이 "" 큰 따옴표가 걸리적 거리기에 반드시 asText로 변환해야 한다.
		String accessToken = jsonToken.get("response").get("access_token").asText();

		// 파라미터로 받아온 imp_uid와 IamPort로부터 전달받은 access_token으로 이번엔 IamPort를 통해 인증된 유저정보를 받아와야 하기에 IamPort 서버와 통신하는 메소드에 imp_uid와 access_token을 전달한다.
		JsonNode userInfo = IamPortPass.getUserInfo(impUid, accessToken);
		// IamPort로부터 반환받은 값에서 필요한 유저정보인 이름과 생년월일과 휴대폰 번호를 가져온다.
		// 가져온 이름을 이름 변수에 전달한다.
		String name = userInfo.get("response").get("name").asText();
		// 가져온 생년월일을 생년월일 변수에 전달한다.
		String birthday = userInfo.get("response").get("birthday").asText();
		// 가져온 휴대폰 번호를 하이픈 제거해서 휴대폰 번호 변수에 전달한다.
		String phoneNumber = userInfo.get("response").get("phone").asText().replaceAll("-", "");

		// 전달받은 유저정보들을 반환하기 위해 Map을 생성한다.
		HashMap<String, String> userMap = new HashMap<>();

		// 전달받은 이름, 생년월일, 휴대폰 번호로 가입자인지 체크한다.
		Sign sign = signService.findByNameAndBirthdayAndPhoneNumber(name, birthday, phoneNumber);

		// 가입자인 경우
		if ( sign != null ) {
			// name을 키로 사용하고, 에러 코드를 값으로 사용하여, 유저정보 Map에 추가한다.
			userMap.put("name", "-1");

			// 에러 코드가 들어간 유저정보 Map을 반환한다.
			return userMap;
		}

		// 가입자가 아닌 경우

		// 전달받은 이름을 암호화한다.
		String passwordName = passwordEncoder.encode(name);
		// 전달받은 생년월일을 암호화한다.
		String passwordBirthday = passwordEncoder.encode(birthday);
		// 전달받은 휴대폰 번호를 암호화한다.
		String passwordPhoneNumber = passwordEncoder.encode(phoneNumber);

		// Certification 쿠키에 암호화된 이름과 암호화된 생년월일과 암호화된 휴대폰 번호를 합쳐서 부여한다.
		Cookie certificationCookie = new Cookie("Certification", passwordName + ":" + passwordBirthday + ":" + passwordPhoneNumber);
		// Certification 쿠키의 만료 시간을 5분으로 설정한다.
		certificationCookie.setMaxAge(300);
		// Certification 쿠키에 HttpOnly를 설정한다. - JavaScript를 통한 접근을 차단
		certificationCookie.setHttpOnly(true);
		// 암호화된 이름과 암호화된 생년월일과 암호화된 휴대폰 번호가 합쳐서 부여된 Certification 쿠키를 추가한다.
		response.addCookie(certificationCookie);

		// name을 키로 사용하고, 전달받은 이름을 값으로 사용하여, 유저정보 Map에 추가한다.
		userMap.put("name", name);
		// name을 키로 사용하고, 전달받은 생년월일을 값으로 사용하여, 유저정보 Map에 추가한다.
		userMap.put("birthday", birthday);
		// name을 키로 사용하고, 전달받은 휴대폰 번호를 값으로 사용하여, 유저정보 Map에 추가한다.
		userMap.put("phoneNumber", phoneNumber);

		// 32. 유저정보가 들어간 유저정보 Map을 반환한다.
		return userMap;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////아이디 및 비밀번호 찾기
	// 휴대폰 인증
	@RequestMapping("/phone_send")
	@ResponseBody
	public String phoneSend(String phoneNumber) throws JSONException {
		// 휴대폰 번호 하이픈 제거
		phoneNumber = phoneNumber.replaceAll("-", "");
		// 휴대폰 인증번호 생성 및 전송
		String smsKey = PhoneKey.randomSmsKey(phoneNumber, smsPhoneNumber, naverAccessKey, naverSecretKey, naverSensKey);
		// 생성한 휴대폰 인증번호 암호화
		String result = passwordEncoder.encode(smsKey);
		// 암호회한 휴대폰 인증번호 전달
		return result;
	}
	// 휴대폰 인증 체크
	@RequestMapping("/phone_check")
	@ResponseBody
	public boolean phoneCheck(String phoneKey, String hPhoneKey) {
		// 암호화된 휴대폰 인증번호와 입력한 휴대폰 인증번호가 일치하는지 체크한다.
		boolean isMatch = passwordEncoder.matches(phoneKey, hPhoneKey);
		// 반환된 결과 값(true/false)을 클라이언트로 반환한다.
		// true - 휴대폰 인증번호 일치 / false - 휴대폰 인증번호 불일치
		return isMatch;
	}

	// ID 찾기 페이지
	@RequestMapping("/find_id")
	public String findID() {
		return "Sign/find_id";
	}
	// ID 찾기
	@RequestMapping("/find_id_check")
	@ResponseBody
	public String findIdCheck(Sign sign) {
		// 휴대폰 번호 하이픈 제거
		sign.setPhoneNumber(sign.getPhoneNumber().replaceAll("-", ""));

		// 이름 + 휴대폰 번호에 해당하는 유저정보가 존재하는지 조회하여 체크한다.
		Sign join = signService.findByNameAndPhoneNumber(sign);
		if ( join == null ) {
			// 조회한 유저정보가 존재하지 않는 경우 "no"를 전달
			return "no";
		}

		// 조회한 유저정보가 존재하는 경우 조회한 유저정보 중 이메일을 전달
		return join.getEmail();
	}
	
	// PW 찾기 페이지
	@RequestMapping("/find_pw")
	public String findPW() {
		return "Sign/find_pw";
	}
	// PW 찾기
	@RequestMapping("/find_pw_send_email")
	@ResponseBody
	public String findPwCheck(Sign sign) {
		// 이메일 + 이름 + 휴대폰 번호에 해당하는 유저정보가 존재하는지 조회하여 체크한다.
		Sign join = signService.findByEmailAndNameAndPhoneNumber(sign);
		if ( join == null ) {
			// 조회한 유저정보가 존재히자 않는 경우 "no"를 전달
			return "no";
		}

		// 유저정보가 존재하는 경우

		// 조회한 유저정보 중 비밀번호를 가져가는게 아닌 임시 비밀번호를 생성해 발급한다.
		// 미리 만들어둔 랜덤키 생성 메소드를 mail패키지의 MailKey.java에서 가져와 사용한다
		String emailKey = new MailKey().getKey(10, false); // 랜덤키 길이 설정
		// Mail Server 설정
		String charSet = "UTF-8"; // 사용할 언어셋
		String hostSMTP = "smtp.naver.com"; // 사용할 SMTP
		String hostSMTPid = naverId; // 사용할 SMTP에 해당하는 ID - 이메일 형식
		String hostSMTPpwd = naverPwd; // 사용할 ID에 해당하는 PWD
		
		// 가장 중요한 TLS설정 - 이것이 없으면 신뢰성 에러가 나온다
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		
		// 보내는 사람 E-Mail, 제목, 내용 
		String fromEmail = naverId; // 보내는 사람 email - - hostSMTPid와 동일하게 작성
		String fromName = "관리자"; // 보내는 사람 이름
		String subject = "[Cyworld] 임시 비밀번호 발급 안내입니다."; // 제목
		
		// 받는 사람 E-Mail 주소
		String mail = sign.getEmail(); // 받는 사람 email
		
		try {
			HtmlEmail email = new HtmlEmail(); // Email 생성
			email.setDebug(true);
			email.setCharset(charSet); // 언어셋 사용
			email.setSSL(true);
			email.setHostName(hostSMTP); // SMTP 사용
			email.setSmtpPort(587);	// SMTP 포트 번호 입력
			
			email.setAuthentication(hostSMTPid, hostSMTPpwd); // 메일 ID, PWD
			email.setTLS(true);
			email.addTo(mail); // 받는 사람
			email.setFrom(fromEmail, fromName, charSet); // 보내는 사람
			email.setSubject(subject); // 제목
			email.setHtmlMsg(
					"<p>" + "[임시 비밀번호 안내입니다.]" + "</p>" +
					"<p>" + "Cyworld를 이용해 주셔서 감사합니다." + "</p>" +
					"<p>" + "아래 임시 비밀번호를 이용해 로그인 후 프로필 변경에서 비밀번호를 재설정해 주세요." + "</p>" +
					"<p>" + "[" + emailKey + "]" + "</p>"); // 본문 내용
			email.send(); // 메일 보내기
			
			/* HashMap - java.util.HashMap
			 * 메일 보내기가 성공하면 해당 ID의 비밀번호에 랜덤키를 보내야 하는데,
			 * 그럼 ID도 가져가야 하고 랜덤키도 가져가야 한다
			 * 하지만 ID는 SignUpVO에 들어 있고, 랜덤키는 MailKey에 들어 있기에
			 * 이 두 가지 정보를 하나로 통합해서 가져가고 싶다면
			 * Map을 생성해서 두 가지 정보를 다 담아서 가져가면 된다
			 * Tip: 키 타입으로 String을 사용하는 게 대체로 편리하다
			 * Tip: 나중에 VO를 통째로 가져갈때는 value 타입을 Object로 사용한다
			 * Map구조 파악용
			 * HashMap<String, Object> map = new HashMap<String, Object>();
			 * map.put("a", vo);
			 * map.put("b", mail_key);
			 */
			HashMap<String, String> m_key = new HashMap<>();
			m_key.put("1", sign.getEmail()); // 1번 키에 이메일을 추가한다.
			m_key.put("2", passwordEncoder.encode(emailKey)); // 2번 키에 임시 비밀번호를 암호화하여 추가한다.
			// 현재 비밀번호를 암호화된 임시 비밀번호로 갱신한다.
			signService.updateSetInfoByEmail(m_key);
			// 메일 보내기가 성공하면 "success"를 전달
			return "success";
		} catch (Exception e) {
			System.out.println(e);
			// 메일 보내기가 실패하면 "false"를 전달
			return "false";
		}
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////로그인 및 회원가입
	// 로그인 체크
	@RequestMapping("/login_check")
	@ResponseBody
	public String loginCheck(Sign sign) {
		// 이메일에 해당하는 유저정보가 존재하는지 조회하여 체크한다.
		Sign login = signService.findByEmail(sign.getEmail());

		// 조회한 유저정보가 존재하지 않는 경우
		if ( login == null ) {
			// json 형식의 에러 메시지 전달
			return "{'result':'no_id'}";
		}

		// 조회한 유저정보가 존재하는 경우

		// 조회한 유저정보 중 암호회된 비밀번호와 입력한 비밀번호가 일치하는지 체크한다.
		// 비밀번호가 일치하지 않는 경우
		if (!passwordEncoder.matches(sign.getInfo(), login.getInfo())) {
			// json 형식의 에러 메시지 전달
			return "{'result':'no_info'}";
		}

		// 비밀번호가 일치하는 경우

		// json 형식의 성공 메시지 전달
		return "{'result':'clear'}";
	}
	
	// 회원가입 시 추가적으로 더 필요한 정보를 넣기 위한 장소
	@RequestMapping("/welcome")
	@ResponseBody
	public String welcome(JoinDTO joinDTO, Model model) {
		// 회원가입 DTO로 가져온 가입정보를 Entity로 변환한다.
		Sign sign = joinDTO.toEntity();

		// 접속 날짜에 가입 날짜를 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다.
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");

		// 휴대폰 번호 하이픈 제거
		sign.setPhoneNumber(sign.getPhoneNumber().replaceAll("-", ""));

		// cyworld 회원가입자가 들어오는 경우
		if ( joinDTO.getPlatform().equals("cyworld") ) {
			////////////////////////////////////////////////////////////////////////////////////////////////////////////이메일 인증정보 체크
			// 쿠키에 들어있는 이메일 인증정보가 가입정보와 일치하는지 체크한다.
			// 쿠키에 들어있는 이메일 인증정보 값
			String[] emailCredential = null;
			// 이메일 인증정보 중 암호화된 이메일 값
			String email = null;
			// 이메일 인증정보 중 암호화된 이메일 인증번호 값
			String emailKey = null;
			// Email 쿠키에 이메일 인증정보가 존재하는지 체크한다.
			Cookie[] emailCookies = request.getCookies();
			// Email 쿠키가 존재하는 경우
			if ( emailCookies != null ) {
				// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
				for ( Cookie cookie : emailCookies ) {
					// Email 쿠키에 이메일 인증정보가 존재하는 경우
					if ( cookie.getName().equals("Email") ) {
						// Email 쿠키에 존재하는 이메일 인증정보를 가져올 배열을 생성한다.
						emailCredential = new String[2];
						// Email 쿠키에 존재하는 이메일 인증정보를 가져온다.
						emailCredential = cookie.getValue().split(":");
						// 이메일 인증정보 중 암호화된 이메일을 가져온다.
						email = emailCredential[0];
						// 이메일 인증정보 중 암호화된 이메일 인증번호를 가져온다.
						emailKey = emailCredential[1];
						// foreach문을 빠져나간다.
						break;
					}
				}
			}

			// Email 쿠키에 이메일 인증정보가 정상적으로 존재하는지 체크한다.
			// Email 쿠키가 만료된 경우
			if ( emailCredential == null ) {
				// 에러 코드를 반환한다.
				return "0";
			}
			// 이메일 인증정보가 손상된 경우
			if ( email == null || emailKey == null ) {
				// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteEmailCookie = new Cookie("Email", "");
				// Email 쿠키의 만료 시간을 0으로 설정한다.
				deleteEmailCookie.setMaxAge(0);
				// 삭제할 Email 쿠키를 추가한다.
				response.addCookie(deleteEmailCookie);

				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);

				// 에러 코드를 반환한다.
				return "-1";
			}

			// 이메일 인증정보가 정상적인 경우

			// 가입정보와 이메일 인증정보가 일치하는지 체크한다.
			// 가입정보 중 이메일과 이메일 인증정보 중 암호화된 이메일이 일치하지 않는 경우
			if (!passwordEncoder.matches(joinDTO.getEmail(), email)) {
				// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteEmailCookie = new Cookie("Email", "");
				// Email 쿠키의 만료 시간을 0으로 설정한다.
				deleteEmailCookie.setMaxAge(0);
				// 삭제할 Email 쿠키를 추가한다.
				response.addCookie(deleteEmailCookie);

				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);

				// 에러 코드를 반환한다.
				return "-1";
			}
			// 가입정보 중 이메일 인증번호와 이메일 인증정보 중 암호화된 이메일 인증번호가 일치하지 않는 경우
			if (!passwordEncoder.matches(joinDTO.getEmailKey(), emailKey)) {
				// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteEmailCookie = new Cookie("Email", "");
				// Email 쿠키의 만료 시간을 0으로 설정한다.
				deleteEmailCookie.setMaxAge(0);
				// 삭제할 Email 쿠키를 추가한다.
				response.addCookie(deleteEmailCookie);

				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);

				// 에러 코드를 반환한다.
				return "-1";
			}

			// 가입정보와 이메일 인증정보가 일치하는 경우

			// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
			Cookie deleteEmailCookie = new Cookie("Email", "");
			// Email 쿠키의 만료 시간을 0으로 설정한다.
			deleteEmailCookie.setMaxAge(0);
			// 삭제할 Email 쿠키를 추가한다.
			response.addCookie(deleteEmailCookie);
			////////////////////////////////////////////////////////////////////////////////////////////////////////////본인인증정보 체크
			// 쿠키에 들어있는 본인인증정보가 가입정보와 일치하는지 체크한다.
			// 쿠키에 들어있는 본인인증정보가 값
			String[] certification = null;
			// 본인인증정보 중 암호화된 이름 값
			String name = null;
			// 본인인증정보 중 암호화된 생년월일 값
			String birthday = null;
			// 본인인증정보 중 암호화된 휴대폰 번호 값
			String phoneNumber = null;
			// Certification 쿠키에 본인인증정보가 존재하는지 체크한다.
			Cookie[] certificationCookies = request.getCookies();
			// Certification 쿠키가 존재하는 경우
			if ( certificationCookies != null ) {
				// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
				for ( Cookie cookie : certificationCookies ) {
					// Certification 쿠키에 본인인증정보가 존재하는 경우
					if ( cookie.getName().equals("Certification") ) {
						// Certification 쿠키에 존재하는 본인인증정보를 가져올 배열을 생성한다.
						certification = new String[3];
						// Certification 쿠키에 존재하는 본인인증정보를 가져온다.
						certification = cookie.getValue().split(":");
						// 본인인증정보 중 암호화된 이름을 가져온다.
						name = certification[0];
						// 본인인증정보 중 암호화된 생년월일을 가져온다.
						birthday = certification[1];
						// 본인인증정보 중 암호화된 휴대폰 번호를 가져온다.
						phoneNumber = certification[2];
						// foreach문을 빠져나간다.
						break;
					}
				}
			}

			// Certification 쿠키에 본인인증정보가 정상적으로 존재하는지 체크한다.
			// Certification 쿠키가 만료된 경우
			if ( certification == null ) {
				// 에러 코드를 반환한다.
				return "0";
			}
			// 본인인증정보가 손상된 경우
			if ( name == null || birthday == null || phoneNumber == null ) {
				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);

				// 에러 코드를 반환한다.
				return "-2";
			}

			// 본인인증정보가 정상적인 경우

			// 가입정보와 본인인증정보가 일치하는지 체크한다.
			// 가입정보 중 이름과 본인인증정보 중 암호화된 이름이 일치하지 않는 경우
			if (!passwordEncoder.matches(joinDTO.getName(), name)) {
				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);

				// 에러 코드를 반환한다.
				return "-2";
			}
			// 가입정보 중 생년월일과 본인인증정보 중 암호화된 생년월일이 일치하지 않는 경우
			if (!passwordEncoder.matches(joinDTO.getBirthday(), birthday)) {
				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);

				// 에러 코드를 반환한다.
				return "-2";
			}
			// 가입정보 중 휴대폰 번호와 본인인증정보 중 암호화된 휴대폰 번호가 일치하지 않는 경우
			if (!passwordEncoder.matches(joinDTO.getPhoneNumber(), phoneNumber)) {
				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);

				// 에러 코드를 반환한다.
				return "-2";
			}

			// 가입정보와 본인인증정보가 일치하는 경우

			// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
			Cookie deleteCertificationCookie = new Cookie("Certification", "");
			// Certification 쿠키의 만료 시간을 0으로 설정한다.
			deleteCertificationCookie.setMaxAge(0);
			// 삭제할 Certification 쿠키를 추가한다.
			response.addCookie(deleteCertificationCookie);
			////////////////////////////////////////////////////////////////////////////////////////////////////////////가입 진행
			// 비밀번호 암호화
			sign.setInfo(passwordEncoder.encode(sign.getInfo()));
			// 추가 정보들을 임의로 지정
			sign.setIdx(null); // AUTO_INCREMENT로 null값 지정시 자동 인덱스 증가
			sign.setMinimi("mainMinimi.png"); // 기본 미니미 지정
			sign.setDotory(0); // 기본 도토리 개수 지정
			sign.setMainTitle("안녕하세요~ " + sign.getName() + "님의 미니홈피입니다!"); // 메인 화면 제목
			sign.setMainPhoto("noImage"); // 메인 화면 사진 지정
			sign.setMainText(sign.getName() + "님의 미니홈피에 오신걸 환영합니다!"); // 메인 화면 소개글
			sign.setIlchon(0); // 일촌 수 지정
			sign.setToday(0); // 일일 조회수
			sign.setTotal(0); // 누적 조회수
			sign.setToDate(today.format(date)); // 접속 날짜 ( 가입 날짜 )
			sign.setConsent(0); // 동의 항목
			// 저장 실패하는 경우
			String result = "no";
			// 가입 유저정보 저장
			Sign join = signService.insertIntoSign(sign);
			if ( join != null ) {
				// 저장 성공하는 경우
				result = "yes";
			}
			// 콜백메소드에 전달
			return result;
		// 소셜 회원가입자가 들어오는 경우
		} else {
			// kakao 회원가입자가 들어오는 경우
			if ( joinDTO.getPlatform().equals("kakao") ) {
				////////////////////////////////////////////////////////////////////////////////////////////////////////본인인증정보 체크
				// 쿠키에 들어있는 본인인증정보가 가입정보와 일치하는지 체크한다.
				// 쿠키에 들어있는 본인인증정보가 값
				String[] certification = null;
				// 본인인증정보 중 암호화된 이름 값
				String name = null;
				// 본인인증정보 중 암호화된 생년월일 값
				String birthday = null;
				// 본인인증정보 중 암호화된 휴대폰 번호 값
				String phoneNumber = null;
				// Certification 쿠키에 본인인증정보가 존재하는지 체크한다.
				Cookie[] certificationCookies = request.getCookies();
				// Certification 쿠키가 존재하는 경우
				if ( certificationCookies != null ) {
					// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
					for ( Cookie cookie : certificationCookies ) {
						// Certification 쿠키에 본인인증정보가 존재하는 경우
						if ( cookie.getName().equals("Certification") ) {
							// Certification 쿠키에 존재하는 본인인증정보를 가져올 배열을 생성한다.
							certification = new String[3];
							// Certification 쿠키에 존재하는 본인인증정보를 가져온다.
							certification = cookie.getValue().split(":");
							// 본인인증정보 중 암호화된 이름을 가져온다.
							name = certification[0];
							// 본인인증정보 중 암호화된 생년월일을 가져온다.
							birthday = certification[1];
							// 본인인증정보 중 암호화된 휴대폰 번호를 가져온다.
							phoneNumber = certification[2];
							// foreach문을 빠져나간다.
							break;
						}
					}
				}

				// Certification 쿠키에 본인인증정보가 정상적으로 존재하는지 체크한다.
				// Certification 쿠키가 만료된 경우
				if ( certification == null ) {
					// 에러 코드를 반환한다.
					return "0";
				}
				// 본인인증정보가 손상된 경우
				if ( name == null || birthday == null || phoneNumber == null ) {
					// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCertificationCookie = new Cookie("Certification", "");
					// Certification 쿠키의 만료 시간을 0으로 설정한다.
					deleteCertificationCookie.setMaxAge(0);
					// 삭제할 Certification 쿠키를 추가한다.
					response.addCookie(deleteCertificationCookie);

					// 에러 코드를 반환한다.
					return "-2";
				}

				// 본인인증정보가 정상적인 경우

				// 가입정보와 본인인증정보가 일치하는지 체크한다.
				// 가입정보 중 이름과 본인인증정보 중 암호화된 이름이 일치하지 않는 경우
				if (!passwordEncoder.matches(joinDTO.getName(), name)) {
					// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCertificationCookie = new Cookie("Certification", "");
					// Certification 쿠키의 만료 시간을 0으로 설정한다.
					deleteCertificationCookie.setMaxAge(0);
					// 삭제할 Certification 쿠키를 추가한다.
					response.addCookie(deleteCertificationCookie);

					// 에러 코드를 반환한다.
					return "-2";
				}
				// 가입정보 중 생년월일과 본인인증정보 중 암호화된 생년월일이 일치하지 않는 경우
				if (!passwordEncoder.matches(joinDTO.getBirthday(), birthday)) {
					// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCertificationCookie = new Cookie("Certification", "");
					// Certification 쿠키의 만료 시간을 0으로 설정한다.
					deleteCertificationCookie.setMaxAge(0);
					// 삭제할 Certification 쿠키를 추가한다.
					response.addCookie(deleteCertificationCookie);

					// 에러 코드를 반환한다.
					return "-2";
				}
				// 가입정보 중 휴대폰 번호와 본인인증정보 중 암호화된 휴대폰 번호가 일치하지 않는 경우
				if (!passwordEncoder.matches(joinDTO.getPhoneNumber(), phoneNumber)) {
					// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteCertificationCookie = new Cookie("Certification", "");
					// Certification 쿠키의 만료 시간을 0으로 설정한다.
					deleteCertificationCookie.setMaxAge(0);
					// 삭제할 Certification 쿠키를 추가한다.
					response.addCookie(deleteCertificationCookie);

					// 에러 코드를 반환한다.
					return "-2";
				}

				// 가입정보와 본인인증정보가 일치하는 경우

				// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
				Cookie deleteCertificationCookie = new Cookie("Certification", "");
				// Certification 쿠키의 만료 시간을 0으로 설정한다.
				deleteCertificationCookie.setMaxAge(0);
				// 삭제할 Certification 쿠키를 추가한다.
				response.addCookie(deleteCertificationCookie);
			}
			// naver 회원가입자가 들어오는 경우
			if ( joinDTO.getPlatform().equals("naver") ) {
				// 이메일 인증하고 들어오는 경우
				if ( joinDTO.getEmailKey() != null ) {
					////////////////////////////////////////////////////////////////////////////////////////////////////이메일 인증정보 체크
					// 쿠키에 들어있는 이메일 인증정보가 가입정보와 일치하는지 체크한다.
					// 쿠키에 들어있는 이메일 인증정보 값
					String[] emailCredential = null;
					// 이메일 인증정보 중 암호화된 이메일 값
					String email = null;
					// 이메일 인증정보 중 암호화된 이메일 인증번호 값
					String emailKey = null;
					// Email 쿠키에 이메일 인증정보가 존재하는지 체크한다.
					Cookie[] emailCookies = request.getCookies();
					// Email 쿠키가 존재하는 경우
					if ( emailCookies != null ) {
						// 쿠키는 name-value로 이루어져 있기에 foreach를 돌린다.
						for ( Cookie cookie : emailCookies ) {
							// Email 쿠키에 이메일 인증정보가 존재하는 경우
							if ( cookie.getName().equals("Email") ) {
								// Email 쿠키에 존재하는 이메일 인증정보를 가져올 배열을 생성한다.
								emailCredential = new String[2];
								// Email 쿠키에 존재하는 이메일 인증정보를 가져온다.
								emailCredential = cookie.getValue().split(":");
								// 이메일 인증정보 중 암호화된 이메일을 가져온다.
								email = emailCredential[0];
								// 이메일 인증정보 중 암호화된 이메일 인증번호를 가져온다.
								emailKey = emailCredential[1];
								// foreach문을 빠져나간다.
								break;
							}
						}
					}

					// Email 쿠키에 이메일 인증정보가 정상적으로 존재하는지 체크한다.
					// Email 쿠키가 만료된 경우
					if ( emailCredential == null ) {
						// 에러 코드를 반환한다.
						return "0";
					}
					// 이메일 인증정보가 손상된 경우
					if ( email == null || emailKey == null ) {
						// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
						Cookie deleteEmailCookie = new Cookie("Email", "");
						// Email 쿠키의 만료 시간을 0으로 설정한다.
						deleteEmailCookie.setMaxAge(0);
						// 삭제할 Email 쿠키를 추가한다.
						response.addCookie(deleteEmailCookie);

						// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
						Cookie deleteCertificationCookie = new Cookie("Certification", "");
						// Certification 쿠키의 만료 시간을 0으로 설정한다.
						deleteCertificationCookie.setMaxAge(0);
						// 삭제할 Certification 쿠키를 추가한다.
						response.addCookie(deleteCertificationCookie);

						// 에러 코드를 반환한다.
						return "-1";
					}

					// 이메일 인증정보가 정상적인 경우

					// 가입정보와 이메일 인증정보가 일치하는지 체크한다.
					// 가입정보 중 이메일과 이메일 인증정보 중 암호화된 이메일이 일치하지 않는 경우
					if (!passwordEncoder.matches(joinDTO.getEmail(), email)) {
						// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
						Cookie deleteEmailCookie = new Cookie("Email", "");
						// Email 쿠키의 만료 시간을 0으로 설정한다.
						deleteEmailCookie.setMaxAge(0);
						// 삭제할 Email 쿠키를 추가한다.
						response.addCookie(deleteEmailCookie);

						// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
						Cookie deleteCertificationCookie = new Cookie("Certification", "");
						// Certification 쿠키의 만료 시간을 0으로 설정한다.
						deleteCertificationCookie.setMaxAge(0);
						// 삭제할 Certification 쿠키를 추가한다.
						response.addCookie(deleteCertificationCookie);

						// 에러 코드를 반환한다.
						return "-1";
					}
					// 가입정보 중 이메일 인증번호와 이메일 인증정보 중 암호화된 이메일 인증번호가 일치하지 않는 경우
					if (!passwordEncoder.matches(joinDTO.getEmailKey(), emailKey)) {
						// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
						Cookie deleteEmailCookie = new Cookie("Email", "");
						// Email 쿠키의 만료 시간을 0으로 설정한다.
						deleteEmailCookie.setMaxAge(0);
						// 삭제할 Email 쿠키를 추가한다.
						response.addCookie(deleteEmailCookie);

						// Certification 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
						Cookie deleteCertificationCookie = new Cookie("Certification", "");
						// Certification 쿠키의 만료 시간을 0으로 설정한다.
						deleteCertificationCookie.setMaxAge(0);
						// 삭제할 Certification 쿠키를 추가한다.
						response.addCookie(deleteCertificationCookie);

						// 에러 코드를 반환한다.
						return "-1";
					}

					// 가입정보와 이메일 인증정보가 일치하는 경우

					// Email 쿠키 삭제를 위해 같은 이름으로 쿠키를 생성한다. - 값은 필요 X
					Cookie deleteEmailCookie = new Cookie("Email", "");
					// Email 쿠키의 만료 시간을 0으로 설정한다.
					deleteEmailCookie.setMaxAge(0);
					// 삭제할 Email 쿠키를 추가한다.
					response.addCookie(deleteEmailCookie);
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////이메일 중복 체크
				// 네이버 로그인 API에서 이메일을 가져오는 방식이 변경됨에 따라 이메일에 해당하는 유저정보가 존재하는지 조회하여 체크한다.
				Sign emailCheck = signService.findByEmail(joinDTO.getEmail());

				// 조회한 유저정보가 존재하는 경우
				if ( emailCheck != null ) {
					// 에러 코드 전달
					return "-3";
				}

				// 조회한 유저정보가 존재하지 않는 경우

			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////가입 진행
			// 추가 정보들을 임의로 지정
			sign.setIdx(null); // AUTO_INCREMENT로 null값 지정시 자동 인덱스 증가
			sign.setInfo(passwordEncoder.encode(UUID.randomUUID().toString())); // 소셜 가입자는 비밀번호가 따로 없으므로 비밀번호에 중복 가능성이 매우 적은 UUID로 랜덤 지정하여 암호화한다.
			sign.setMinimi("mainMinimi.png"); // 기본 미니미 지정
			sign.setDotory(0); // 기본 도토리 개수 지정
			sign.setMainTitle("안녕하세요~ " + sign.getName() + " 님의 미니홈피입니다!"); // 메인 화면 제목
			sign.setMainPhoto("noImage"); // 메인 화면 사진 지정
			sign.setMainText(sign.getName() + "님의 미니홈피에 오신걸 환영합니다!"); // 메인 화면 소개글
			sign.setIlchon(0); // 일촌 수 지정
			sign.setToday(0); // 일일 조회수
			sign.setTotal(0); // 누적 조회수
			sign.setToDate(today.format(date)); // 접속 날짜 ( 가입 날짜 )
			sign.setConsent(0); // 동의 항목
			// 저장 실패하는 경우
			String result = "no";
			// 가입 유저정보 저장
			Sign join = signService.insertIntoSign(sign);
			if ( join != null ) {
				// 저장 성공하는 경우
				result = "yes";
			}
			// 콜백 메소드에 전달
			return result;
		}
	}
}