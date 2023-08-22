package com.social.cyworld.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.social.cyworld.dto.ApiDTO;
import com.social.cyworld.entity.ApiConsent;
import com.social.cyworld.entity.ApiKey;
import com.social.cyworld.entity.Sign;
import com.social.cyworld.service.ApiService;
import com.social.cyworld.service.ProfileService;
import com.social.cyworld.service.SignService;
import com.social.cyworld.util.ApiToken;
import com.social.cyworld.util.CodeExpirationManager;
import com.social.cyworld.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/***********************************************************************************************************************
 Cyworld Login API 가이드
 -----------------------------------------------------------------------------------------------------------------------API Login Code 발급
 GET http://localhost:9999/api/cyworld - API 검증 --> API 로그인 --> API 동의 항목 --> API Login Code 발급
 "?clientId=" + Client ID Key + "&redirectUri=" + Redirect URI
 정상
 Redirect URI + "?code=" + API Login Code
 에러 - API 에러 페이지
 http://localhost:9999/api/error + "?code=" + Error Message
 -----------------------------------------------------------------------------------------------------------------------API Access Token 발급
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
 -----------------------------------------------------------------------------------------------------------------------API 유저 정보 조회
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
 -----------------------------------------------------------------------------------------------------------------------
 ***********************************************************************************************************************/

@PropertySource("classpath:application-information.properties")
@RequestMapping("/api")
@Controller
public class ApiController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	HttpServletResponse response;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	ApiToken apiToken;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	SignService signService;
	@Autowired
	ProfileService profileService;
	@Autowired
	ApiService apiService;

	// 5분간 유지되는 API Login Code 저장 Map
	private CodeExpirationManager codeExpirationManager = new CodeExpirationManager();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////API Key 발급
	// API 팝업 이동
	@RequestMapping("/profile_api_popup.do")
	public String apiPopUp(int idx, Model model) {
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
				// 해당 미니홈피 유저의 메인 페이지로 이동
				return "redirect:main.do?idx=" + idx;
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 로그인 페이지로 이동
				return "redirect:login.do";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// API 팝업으로 이동
			return "Page/api_popup";
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
				// API 팝업으로 이동
				return "Page/api_popup";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// API 팝업으로 이동
					return "Page/api_popup";
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

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 프로필은 오로지 미니홈피 주인만 들어갈 수 있다
		if ( loginIdx != idx ) {
			// 해당 미니홈피 유저의 메인 페이지로 이동
			return "redirect:main.do?idx=" + idx;
		}

		// API Key를 조회한다.
		ApiKey apiKey = apiService.findByApiKeyIdx(loginIdx);

		// 조회한 API Key가 존재한다면 API Key가 바인딩 되고, 존재하지 않다면 null이 바인딩 된다.
		model.addAttribute("apiKey", apiKey);
		// 로그인 유저 idx를 바인딩한다.
		model.addAttribute("loginIdx", loginIdx);

		// API 팝업으로 이동
		return "Page/api_popup";
	}

	// API Key 발급
	@RequestMapping("/profile_api_key.do")
	public String apiKey(int idx, Model model) {
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
				// 해당 미니홈피 유저의 메인 페이지로 이동
				return "redirect:main.do?idx=" + idx;
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 로그인 페이지로 이동
				return "redirect:login.do";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// API 팝업으로 이동
			return "Page/api_popup";
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
				// API 팝업으로 이동
				return "Page/api_popup";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// API 팝업으로 이동
					return "Page/api_popup";
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

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 프로필은 오로지 미니홈피 주인만 들어갈 수 있다
		if ( loginIdx != idx ) {
			// 해당 미니홈피 유저의 메인 페이지로 이동
			return "redirect:main.do?idx=" + idx;
		}

		// API Key 객체를 생성한다.
		ApiKey apiKey = new ApiKey();
		// API idx에 로그인 유저 idx를 설정한다.
		apiKey.setIdx(loginIdx);
		// API ClientID에 고유한 식별자를 생성하기 위해 UUID (Universally Unique Identifier)를 생성하고, 하이픈(-)을 제거하여 설정한다.
		apiKey.setClientId(UUID.randomUUID().toString().replace("-", ""));
		// API ClientSecret에 고유한 식별자를 생성하기 위해 UUID (Universally Unique Identifier)를 생성하고, 하이픈(-)을 제거하여 설정한다.
		apiKey.setClientSecret(UUID.randomUUID().toString().replace("-", ""));
		// API Scope 성별에 0을 설정한다.
		apiKey.setGender(0);
		// API Scope 이름에 0을 설정한다.
		apiKey.setName(0);
		// API Scope 생년월일에 0을 설정한다.
		apiKey.setBirthday(0);
		// API Scope 휴대폰 번호에 0을 설정한다.
		apiKey.setPhoneNumber(0);
		// API Scope 이메일에 0을 설정한다.
		apiKey.setEmail(0);

		// 값들이 설정된 API Key 객체를 저장한다. - API Key 발급
		apiService.insertIntoApiKey(apiKey);

		// API 팝업으로 이동
		return "redirect:profile_api_popup.do?idx=" + loginIdx;
	}

	// API RedirectURI 및 동의 항목 설정
	@RequestMapping("/profile_api_check.do")
	@ResponseBody
	public String apiCheck(ApiKey apiKey, Model model) {
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
				// 에러 코드를 반환한다.
				return "-4";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 코드를 반환한다.
				return "0";
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
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 미니홈피 유저 idx가 다른 경우 - 프로필은 오로지 미니홈피 주인만 들어갈 수 있다
		if ( loginIdx != apiKey.getIdx() ) {
			// 해당 미니홈피 유저의 메인 페이지로 이동
			return "redirect:main.do?idx=" + apiKey.getIdx();
		}

		// 저장 or 수정 실패하는 경우
		String result = "no";

		// RedirectURI 및 동의 항목을 저장 or 수정하고 성공 or 실패 체크 값을 받아온다.
		int check = apiService.updateSetRedirectUriAndGenderAndNameAndBirthdayAndPhoneNumberAndEmailByIdx(apiKey);
		if ( check == 1 ) {
			// 저장 or 수정 성공하는 경우
			result = "yes";
		}

		// 결과 값을 반환한다.
		return result;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////API Login
	// API 검증
	@GetMapping("/cyworld")
	public ResponseEntity<String> apiCheck(ApiDTO apiDTO) {
		// RedirectURI에 해당하는 API 정보가 존재하는지 체크한다.
		ApiKey apiKey = apiService.findByRedirectUri(apiDTO.getRedirectUri());

		// API 정보가 존재하지 않는 경우
		if ( apiKey == null ) {
			// API RedirectURI를 재구성한다.
			String cyworldRedirectUri = UriComponentsBuilder // URI를 생성하는 빌더를 생성한다.
					.fromUriString("http://localhost:9999/api/error") // API 에러 페이지 주소를 기반으로 URI 구성을 시작한다.
					.queryParam("code", "Invalid Redirect URI") // code 이름의 쿼리 파라미터에 에러 메시지를 값으로 추가한다.
					.toUriString(); // 구성된 URI를 문자열 형식으로 반환한다.

			// 재구성한 API RedirectURI로 에러 메시지를 반환한다.
			return ResponseEntity.status(HttpStatus.FOUND)	// HTTP 응답 상태 코드를 302 (Found)으로 설정한 ResponseEntity 객체를 생성한다.
															// 이 코드는 일반적으로 리다이렉션을 나타내는데 사용된다.
															// 클라이언트가 이 응답을 받으면, 서버는 Location 헤더에 설정된 URI로 클라이언트를 리다이렉트한다.
					.header("Location", cyworldRedirectUri) // Location 헤더에 재구성한 API RedirectURI 설정한다.
					.build(); // ResponseEntity를 빌드하여 생성한다.
		}

		// API 정보가 존재하는 경우

		// RedirectURI에 해당하는 API 정보 중 ClientID와 파라미터로 가져온 API 정보 중 ClientID가 일치하는지 체크한다.
		// ClientID가 일치하지 않는 경우
		if ( !apiKey.getClientId().equals(apiDTO.getClientId()) ) {
			// API RedirectURI를 재구성한다.
			String cyworldRedirectUri = UriComponentsBuilder // URI를 생성하는 빌더를 생성한다.
					.fromUriString("http://localhost:9999/api/error") // API 에러 페이지 주소를 기반으로 URI 구성을 시작한다.
					.queryParam("code", "Invalid Client ID Key") // code 이름의 쿼리 파라미터에 에러 메시지를 값으로 추가한다.
					.toUriString(); // 구성된 URI를 문자열 형식으로 반환한다.

			// 재구성한 API RedirectURI로 에러 메시지를 반환한다.
			return ResponseEntity.status(HttpStatus.FOUND)	// HTTP 응답 상태 코드를 302 (Found)으로 설정한 ResponseEntity 객체를 생성한다.
															// 이 코드는 일반적으로 리다이렉션을 나타내는데 사용된다.
															// 클라이언트가 이 응답을 받으면, 서버는 Location 헤더에 설정된 URI로 클라이언트를 리다이렉트한다.
					.header("Location", cyworldRedirectUri) // Location 헤더에 재구성한 API RedirectURI 설정한다.
					.build(); // ResponseEntity를 빌드하여 생성한다.
		}

		// ClientID가 일치하는 경우

		// API RedirectURI를 재구성한다.
		String cyworldRedirectUri = UriComponentsBuilder // URI를 생성하는 빌더를 생성한다.
				.fromUriString("http://localhost:9999/api/loginform") // API 로그인 페이지 주소를 기반으로 URI 구성을 시작한다.
				.queryParam("clientId", apiKey.getClientId()) // clientId 이름의 쿼리 파라미터에 RedirectURI에 해당하는 API 정보 중 ClientID를 값으로 추가한다.
				.queryParam("redirectUri", apiKey.getRedirectUri()) // redirectUri 이름의 쿼리 파라미터에 RedirectURI에 해당하는 API 정보 중 RedirectURI를 값으로 추가한다.
				.toUriString(); // 구성된 URI를 문자열 형식으로 반환한다.

		// 재구성한 API RedirectURI로 에러 메시지를 반환한다.
		return ResponseEntity.status(HttpStatus.FOUND)	// HTTP 응답 상태 코드를 302 (Found)으로 설정한 ResponseEntity 객체를 생성한다.
														// 이 코드는 일반적으로 리다이렉션을 나타내는데 사용된다.
														// 클라이언트가 이 응답을 받으면, 서버는 Location 헤더에 설정된 URI로 클라이언트를 리다이렉트한다.
				.header("Location", cyworldRedirectUri) // Location 헤더에 재구성한 API RedirectURI 설정한다.
				.build(); // ResponseEntity를 빌드하여 생성한다.
	}

	// API Login 페이지
	@GetMapping("/loginform")
	public String apiLoginForm(ApiDTO apiDTO, Model model) {
		// 파라미터로 받아온 API 정보를 바인딩한다.
		model.addAttribute("apiDTO", apiDTO);

		// API Login 페이지로 이동
		return "Sign/api_login";
	}

	// API Login 검증
	@PostMapping("/loginform/login")
	@ResponseBody
	public int apiLogin(String userId, String info) {
		// 아이디에 해당하는 유저 정보가 존재하는지 체크한다.
		Sign sign = signService.findByUserId(userId);

		// 유저 정보가 존재하지 않는 경우 - 아이디 X
		if ( sign == null ) {
			// 에러 값을 반환한다.
			return -1;
		}

		// 유저 정보가 존재하는 경우

		// 조회한 유저 정보에서 비빌번호를 가져와 입력한 비밀번호와 일치하는지 체크한다.
		// 비밀번호가 일치하지 않는 경우 - 비밀번호 X
		if ( !passwordEncoder.matches(info, sign.getInfo()) ) {
			// 에러 값을 반환한다.
			return -2;
		}

		// 비밀번호가 일치하는 경우 - 로그인

		// 조죄한 유저 정보에서 동의 항목 체크 값을 가져와 동의 항목 페이지를 거쳤는지 체크한다.
		// 동의 항목에 체크하지 않은 경우
		if ( sign.getConsent() == 0 ) {
			// 로그인 유저 idx를 반환한다.
			return sign.getIdx();
		}

		// 동의 항목에 체크한 경우

		// 성공 값을 반환한다.
		return 0;
	}

	// API 동의 항목 페이지
	@PostMapping("/login/consent")
	public String apiConsent(ApiDTO apiDTO, Model model) {
		// 로그인 유저 idx에 해당하는 API 정보를 조회한다.
		ApiKey apiKey = apiService.findByApiKeyIdx(apiDTO.getIdx());

		// 조회한 API 정보를 바인딩한다.
		model.addAttribute("apiKey", apiKey);

		// API 동의 항목 페이지로 이동
		return "Sign/api_consent";
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////API Login Code
	// API 검증 및 API 동의 항목 체크 검증 이후 API Login Code 발급
	@PostMapping("/login/code")
	public ResponseEntity<String> apiValidationClientID(ApiDTO apiDTO) {
		// RedirectURI에 해당하는 API 정보가 존재하는지 체크한다.
		ApiKey apiKey = apiService.findByRedirectUri(apiDTO.getRedirectUri());

		// API 정보가 존재하지 않는 경우
		if ( apiKey == null ) {
			// API RedirectURI를 재구성한다.
			String cyworldRedirectUri = UriComponentsBuilder // URI를 생성하는 빌더를 생성한다.
					.fromUriString("http://localhost:9999/api/error") // API 에러 페이지 주소를 기반으로 URI 구성을 시작한다.
					.queryParam("code", "Invalid Redirect URI") // code 이름의 쿼리 파라미터에 에러 메시지를 값으로 추가한다.
					.toUriString(); // 구성된 URI를 문자열 형식으로 반환한다.

			// 재구성한 API RedirectURI로 에러 메시지를 반환한다.
			return ResponseEntity.status(HttpStatus.FOUND)	// HTTP 응답 상태 코드를 302 (Found)으로 설정한 ResponseEntity 객체를 생성한다.
															// 이 코드는 일반적으로 리다이렉션을 나타내는데 사용된다.
															// 클라이언트가 이 응답을 받으면, 서버는 Location 헤더에 설정된 URI로 클라이언트를 리다이렉트한다.
					.header("Location", cyworldRedirectUri) // Location 헤더에 재구성한 API RedirectURI 설정한다.
					.build(); // ResponseEntity를 빌드하여 생성한다.
		}

		// API 정보가 존재하는 경우

		// RedirectURI에 해당하는 API 정보 중 ClientID와 파라미터로 가져온 API 정보 중 ClientID가 일치하는지 체크한다.
		// ClientID가 일치하지 않는 경우
		if ( !apiKey.getClientId().equals(apiDTO.getClientId()) ) {
			// API RedirectURI를 재구성한다.
			String cyworldRedirectUri = UriComponentsBuilder // URI를 생성하는 빌더를 생성한다.
					.fromUriString("http://localhost:9999/api/error") // API 에러 페이지 주소를 기반으로 URI 구성을 시작한다.
					.queryParam("code", "Invalid Client ID Key") // code 이름의 쿼리 파라미터에 에러 메시지를 값으로 추가한다.
					.toUriString(); // 구성된 URI를 문자열 형식으로 반환한다.

			// 재구성한 API RedirectURI로 에러 메시지를 반환한다.
			return ResponseEntity.status(HttpStatus.FOUND)	// HTTP 응답 상태 코드를 302 (Found)으로 설정한 ResponseEntity 객체를 생성한다.
															// 이 코드는 일반적으로 리다이렉션을 나타내는데 사용된다.
															// 클라이언트가 이 응답을 받으면, 서버는 Location 헤더에 설정된 URI로 클라이언트를 리다이렉트한다.
					.header("Location", cyworldRedirectUri) // Location 헤더에 재구성한 API RedirectURI 설정한다.
					.build(); // ResponseEntity를 빌드하여 생성한다.
		}

		// ClientID가 일치하는 경우

		// API 동의 항목 체크 검증 값으로 API 동의 항목 페이지를 거쳐서 왔는지 체크한다.
		// API 동의 항목 페이지를 거쳐서 온 경우
		if ( apiDTO.getConsent() != null ) {
			// API DTO로 받아온 API 정보를 ApiConsent 객체로 변환한다.
			ApiConsent apiConsent = apiDTO.toApiConsent();
			// 변환된 ApiConsent 객체로 체크한 동의 항목들을 저장한다.
			apiService.insertIntoApiConsent(apiConsent);
			// 로그인 유저 idx에 해당하는 유저 정보 중 동의 항목 체크 값을 체크 완료 값인 1로 수정한다.
			apiService.updateSetConsentByIdx(apiConsent.getIdx());
		}

		// API 동의 항목 페이지를 거쳐서 오지 않은 경우

		// API Login Code를 생성한다.
		String code = UUID.randomUUID().toString().replace("-", "");
		// API RedirectURI를 재구성한다.
		String cyworldRedirectUri = UriComponentsBuilder // URI를 생성하는 빌더를 생성한다.
				.fromUriString(apiKey.getRedirectUri()) // apiKey.getRedirectUri()를 기반으로 URI 구성을 시작한다.
				.queryParam("code", code) // code 이름의 쿼리 파라미터에 생성한 API Login Code를 값으로 추가한다.
				.toUriString(); // 구성된 URI를 문자열 형식으로 반환한다.
		// API Login Code를 키로 사용하고, API Login Code를 값으로 사용하여 API Login Code Map에 추가하여, 5분간 유지한다.
		codeExpirationManager.putCodeWithExpiration(code, code);

		// 재구성한 API RedirectURI로 API Login Code를 반환한다.
		return ResponseEntity.status(HttpStatus.FOUND)	// HTTP 응답 상태 코드를 302 (Found)으로 설정한 ResponseEntity 객체를 생성한다.
														// 이 코드는 일반적으로 리다이렉션을 나타내는데 사용된다.
														// 클라이언트가 이 응답을 받으면, 서버는 Location 헤더에 설정된 URI로 클라이언트를 리다이렉트한다.
				.header("Location", cyworldRedirectUri) // Location 헤더에 재구성한 API RedirectURI 설정한다.
				.build(); // ResponseEntity를 빌드하여 생성한다.
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////API Access Token
	// API 검증 이후 API Access Token 발급
	@PostMapping("/token")
	public ResponseEntity<String> apiAccessToken(ApiDTO apiDTO) throws UnsupportedEncodingException, JsonProcessingException {
		// RedirectURI에 해당하는 API 정보가 존재하는지 체크한다.
		ApiKey apiKey = apiService.findByRedirectUri(apiDTO.getRedirectUri());

		// API 정보가 존재하지 않는 경우
		if ( apiKey == null ) {
			// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
			ObjectMapper objectMapper = new ObjectMapper();
			// 에러 값들을 전달할 Map을 생성한다.
			Map<String, String> responseMap = new HashMap<>();
			// 생성한 Map에 에러 값들을 추가한다.
			responseMap.put("accessToken", "0"); // accessToken를 키로 사용하고, 에러 코드를 값으로 사용하여 추가한다.
			responseMap.put("message", "Invalid Redirect URI"); // message를 키로 사용하고, 에러 메시지를 값으로 사용하여 추가한다.
			// 에러 값들이 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
			String jsonResponse = objectMapper.writeValueAsString(responseMap);

			// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
			return ResponseEntity.badRequest() // HTTP 응답 상태 코드를 400 (Bad Request)으로 설정한 ResponseEntity 객체를 생성한다.
					.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
					.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
		}

		// API 정보가 존재하는 경우

		// RedirectURI에 해당하는 API 정보 중 ClientID와 파라미터로 가져온 API 정보 중 ClientID가 일치하는지 체크한다.
		// ClientID가 일치하지 않는 경우
		if ( !apiKey.getClientId().equals(apiDTO.getClientId()) ) {
			// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
			ObjectMapper objectMapper = new ObjectMapper();
			// 에러 값들을 전달할 Map을 생성한다.
			Map<String, String> responseMap = new HashMap<>();
			// 생성한 Map에 에러 값들을 추가한다.
			responseMap.put("accessToken", "-1"); // accessToken를 키로 사용하고, 에러 코드를 값으로 사용하여 추가한다.
			responseMap.put("message", "Invalid Client ID Key"); // message를 키로 사용하고, 에러 메시지를 값으로 사용하여 추가한다.
			// 에러 값들이 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
			String jsonResponse = objectMapper.writeValueAsString(responseMap);

			// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
			return ResponseEntity.badRequest() // HTTP 응답 상태 코드를 400 (Bad Request)으로 설정한 ResponseEntity 객체를 생성한다.
					.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
					.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
		}

		// ClientID가 일치하는 경우

		// RedirectURI에 해당하는 API 정보 중 ClientSecret과 파라미터로 가져온 API 정보 중 ClientSecret이 일치하는지 체크한다.
		// ClientSecret이 일치하지 않는 경우
		if ( !apiKey.getClientSecret().equals(apiDTO.getClientSecret()) ) {
			// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
			ObjectMapper objectMapper = new ObjectMapper();
			// 에러 값들을 전달할 Map을 생성한다.
			Map<String, String> responseMap = new HashMap<>();
			// 생성한 Map에 에러 값들을 추가한다.
			responseMap.put("accessToken", "-2"); // accessToken를 키로 사용하고, 에러 코드를 값으로 사용하여 추가한다.
			responseMap.put("message", "Invalid Client Secret Key"); // message를 키로 사용하고, 에러 메시지를 값으로 사용하여 추가한다.
			// 에러 값들이 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
			String jsonResponse = objectMapper.writeValueAsString(responseMap);

			// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
			return ResponseEntity.badRequest() // HTTP 응답 상태 코드를 400 (Bad Request)으로 설정한 ResponseEntity 객체를 생성한다.
					.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
					.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
		}

		// ClientSecret이 일치하는 경우

		// API Login Code Map에 추가한 API Login Code 키와 파라미터로 가져온 API 정보 중 API Login Code가 일치하는지 체크한다.
		String redirectUri = codeExpirationManager.getApiKeyIdx(apiDTO.getCode());

		// API Login Code가 일치하지 않는 경우
		if ( redirectUri == null ) {
			// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
			ObjectMapper objectMapper = new ObjectMapper();
			// 에러 값들을 전달할 Map을 생성한다.
			Map<String, String> responseMap = new HashMap<>();
			// 생성한 Map에 에러 값들을 추가한다.
			responseMap.put("accessToken", "-3"); // accessToken를 키로 사용하고, 에러 코드를 값으로 사용하여 추가한다.
			responseMap.put("message", "Invalid Code"); // message를 키로 사용하고, 에러 메시지를 값으로 사용하여 추가한다.
			// 에러 값들이 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
			String jsonResponse = objectMapper.writeValueAsString(responseMap);

			// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
			return ResponseEntity.badRequest() // HTTP 응답 상태 코드를 400 (Bad Request)으로 설정한 ResponseEntity 객체를 생성한다.
					.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
					.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
		}

		// API Login Code가 일치하는 경우

		// API AccessToken을 발급한다.
		String accessToken = apiToken.createToken(apiKey.getIdx());
		// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
		ObjectMapper objectMapper = new ObjectMapper();
		// API AccessToken을 전달할 Map을 생성한다.
		Map<String, String> responseMap = new HashMap<>();
		// 생성한 Map에 발급받은 API AccessToken을 추가한다.
		responseMap.put("accessToken", accessToken); // accessToken를 키로 사용하고, 발급받은 API AccessToken을 값으로 사용하여 추가한다.
		// API AccessToken이 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
		String jsonResponse = objectMapper.writeValueAsString(responseMap);

		// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
		return ResponseEntity.ok() // HTTP 응답 상태 코드를 200 (OK)으로 설정한 ResponseEntity 객체를 생성한다.
				.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
				.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
	}

	// 토큰 검증 및 유저 정보 전달
	@PostMapping("/user")
	public ResponseEntity<String> user(@RequestHeader("Authorization") String accessToken) throws JsonProcessingException {
		// 파라미터로 받아온 AccessToken에서 필요없어진 앞부분을 잘라낸다.
		accessToken = accessToken.substring("Bearer ".length());
		// JWT에서 AccessToken에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = apiToken.validationToken(accessToken);

		// 로그인 유저 idx가 에러 코드인 경우
		if ( loginIdx <= 0 ) {
			// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
			ObjectMapper objectMapper = new ObjectMapper();
			// 에러 값을 전달할 Map을 생성한다.
			Map<String, Integer> responseMap = new HashMap<>();
			// 생성한 Map에 에러 값을 추가한다.
			responseMap.put("user", loginIdx); // user를 키로 사용하고, 에러 코드를 값으로 사용하여 추가한다.
			// 에러 값이 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
			String jsonResponse = objectMapper.writeValueAsString(responseMap);

			// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
			return ResponseEntity.badRequest() // HTTP 응답 상태 코드를 400 (Bad Request)으로 설정한 ResponseEntity 객체를 생성한다.
					.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
					.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
		}

		// 로그인 유저 idx가 정상인 경우

		// 로그인 유저 idx에 해당하는 유저 정보가 존재하는지 체크한다.
		Sign loginUser = signService.findByIdx(loginIdx);

		// 유저 정보가 존재하지 않는 경우
		if ( loginUser == null ) {
			// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
			ObjectMapper objectMapper = new ObjectMapper();
			// 에러 값을 전달할 Map을 생성한다.
			Map<String, Object> responseMap = new HashMap<>();
			// 생성한 Map에 에러 값을 추가한다.
			responseMap.put("user", null); // user를 키로 사용하고, null을 값으로 사용하여 추가한다.
			// 에러 값이 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
			String jsonResponse = objectMapper.writeValueAsString(responseMap);

			// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
			return ResponseEntity.badRequest() // HTTP 응답 상태 코드를 400 (Bad Request)으로 설정한 ResponseEntity 객체를 생성한다.
					.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
					.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
		}

		// 유저 정보가 존재하는 경우

		// 로그인 유저 idx에 해당하는 API 동의 항목 정보를 조회한다.
		ApiConsent apiConsent = apiService.findByApiConsentIdx(loginIdx);

		// Java 객체를 JSON 형식의 데이터로 직렬화(Serialize) 하기 위해 ObjectMapper를 생성한다.
		ObjectMapper objectMapper = new ObjectMapper();
		// 로그인 유저 정보를 전달할 Map을 생성한다.
		Map<String, String> responseMap = new HashMap<>();
		// 생성한 Map에 로그인 유저 정보 중 동의 항목 페이지에서 체크한 정보들만 추가한다.
		// 성별 동의 항목에 체크한 경우
		if ( apiConsent.getGender() == 5 || apiConsent.getGender() == 3 ) {
			responseMap.put("gender", loginUser.getGender()); // gender를 키로 사용하고, 로그인 유저 성별을 값으로 사용하여 추가한다.
		// 성별 동의 항목에 체크하지 않은 경우
		} else if ( apiConsent.getGender() == 2 ) {
			responseMap.put("gender", null); // gender를 키로 사용하고, null을 값으로 사용하여 추가한다.
		}
		// 이름 동의 항목에 체크한 경우
		if ( apiConsent.getName() == 5 || apiConsent.getName() == 3 ) {
			responseMap.put("name", loginUser.getName()); // name을 키로 사용하고, 로그인 유저 이름을 값으로 사용하여 추가한다.
		// 이름 동의 항목에 체크하지 않은 경우
		} else if ( apiConsent.getName() == 2 ) {
			responseMap.put("name", null); // name을 키로 사용하고, null을 값으로 사용하여 추가한다.
		}
		// 생년월일 동의 항목에 체크한 경우
		if ( apiConsent.getBirthday() == 5 || apiConsent.getBirthday() == 3 ) {
			responseMap.put("birthday", loginUser.getBirthday()); // birthday를 키로 사용하고, 로그인 유저 생년월일을 값으로 사용하여 추가한다.
		// 생년월일 동의 항목에 체크하지 않은 경우
		} else if ( apiConsent.getBirthday() == 2 ) {
			responseMap.put("birthday", null); // birthday를 키로 사용하고, null을 값으로 사용하여 추가한다.
		}
		// 휴대폰 번호 동의 항목에 체크한 경우
		if ( apiConsent.getPhoneNumber() == 5 || apiConsent.getPhoneNumber() == 3 ) {
			responseMap.put("phoneNumber", loginUser.getPhoneNumber()); // phoneNumber를 키로 사용하고, 로그인 유저 휴대폰 번호를 값으로 사용하여 추가한다.
		// 휴대폰 번호 동의 항목에 체크하지 않은 경우
		} else if ( apiConsent.getPhoneNumber() == 2 ) {
			responseMap.put("phoneNumber", null); // phoneNumber를 키로 사용하고, null을 값으로 사용하여 추가한다.
		}
		// 이메일 동의 항목에 체크한 경우
		if ( apiConsent.getEmail() == 5 || apiConsent.getEmail() == 3 ) {
			responseMap.put("email", loginUser.getEmail()); // email을 키로 사용하고, 로그인 유저 이메일을 값으로 사용하여 추가한다.
		// 이메일 동의 항목에 체크하지 않은 경우
		} else if ( apiConsent.getEmail() == 2 ) {
			responseMap.put("email", null); // email을 키로 사용하고, null을 값으로 사용하여 추가한다.
		}
		// 로그인 유저 정보가 추가된 Map을 JSON 형식의 데이터로 직렬화(Serialize) 한다.
		String jsonResponse = objectMapper.writeValueAsString(responseMap);

		// 직렬화(Serialize)한 JSON 형식의 데이터를 반환한다.
		return ResponseEntity.ok() // HTTP 응답 상태 코드를 200 (OK)으로 설정한 ResponseEntity 객체를 생성한다.
				.contentType(MediaType.APPLICATION_JSON) // ResponseEntity 객체의 응답 본문의 미디어 타입을 JSON 형식의 데이터를 나타내는 미디어 타입으로 설정한다.
				.body(jsonResponse); // JSON 형식의 데이터를 Location 헤더 대신 Body에 넣어 반환한다.
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////API Error
	// API 에러 페이지
	@GetMapping("/error")
	public String apiError(String code, Model model) {
		// 에러 메시지를 바인딩한다.
		model.addAttribute("code", code);

		// API 에러 페이지로 이동
		return "Error/api_error";
	}
}