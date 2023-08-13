package com.social.cyworld.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.social.cyworld.entity.*;
import com.social.cyworld.service.GuestbookService;
import com.social.cyworld.service.MainService;
import com.social.cyworld.service.SignService;
import com.social.cyworld.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GuestbookController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	HttpHeaders headers;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	SignService signService;
	@Autowired
	MainService mainService;
	@Autowired
	GuestbookService guestbookService;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 방명록 조회
	@RequestMapping("/guestbook.do")
	public String guestbook_list (int idx, Model model) {
		// Authorization 헤더에 토큰이 존재하는지 체크
		String authorization = headers.getFirst("Authorization");
		// 헤더에 토큰이 존재하지 않는 경우 - 에러
		if ( authorization == null ) {
			// 세션에 값이 존재하는지 체크한다.
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
		// 헤더에 토큰이 존재하는 경우 - 정상
		// JWT의 토큰에 해당하는 idx 추출
		int loginIdx = jwtUtil.validationToken(authorization.substring("Bearer ".length()));
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 메인 페이지로 이동
			return "Page/main";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// JWT의 리프레쉬 토큰으로 토큰 재생성
			String refreshToken = jwtUtil.validationRefreshToken(authorization.substring("Bearer ".length()));
			// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
			// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
			if ( refreshToken == null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 메인 페이지로 이동
				return "Page/main";
			// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
			} else {
				// 세션에 값이 존재하는지 체크한다.
				HttpSession session = request.getSession();
				// 세션에 값이 존재하지 않는 경우 - 대기 시간 1시간 이후
				if ( session.getAttribute("login") == null ) {
					// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
					jwtUtil.timeoutToken(refreshToken);
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 메인 페이지로 이동
					return "Page/main";
				// 세션에 값이 존재하는 경우 - 대기 시간 1시간 이전
				} else {
					// Authorization 헤더에 재생성한 토큰 부여
					headers.add("Authorization", "Bearer " + refreshToken);
					// JWT의 재생성한 토큰에 해당하는 idx 추출
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);
		
		// 조회수 구역 시작 //
		
		// 먼저 접속 날짜를 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
		
		// 그리고 앞으로 사용할 로그인한 유저의 idx와 해당 미니홈피의 idx와 접속 날짜를 편하게 사용하기 위해 Map으로 만들어 둔다
		HashMap<String, Object> todayMap = new HashMap<String, Object>();
		todayMap.put("1", idx); // 해당 미니홈피 유저의 idx
		todayMap.put("2", loginIdx); // 로그인한 유저의 idx
		todayMap.put("3", today.format(date)); // 접속 날짜

		// 로그인 유저 idx가 비회원이 아닐 경우 - 로그인 유저 idx가 비회원일 경우 조회수 증가 X
		if ( loginIdx > 0 ) {

			// 로그인 유저 idx와 미니홈피 유저 idx값이 다를 경우 - 타 유저 미니홈피 조회 - 조회수 증가 O
			if ( loginIdx != idx ) {

				// 그 다음 로그인한 유저가 해당 미니홈피로 방문 기록이 있는지 조회
				Views loginUser = mainService.findByViewsIdxAndViewsSessionIdx(todayMap);

				// 그 다음 idx에 해당하는 미니홈피 유저 정보를 조회
				Sign miniUser = signService.findByIdx(idx);

				// 로그인한 유저의 방문 기록이 있을 경우
				if ( loginUser != null ) {

					// 로그인한 유저의 방문 기록 중 방문 날짜가 현재 날짜와 다를 경우
					if ( !loginUser.getTodayDate().equals(today.format(date)) ) {

						// 로그인한 유저의 해당 미니홈피 방문 날짜를 현재 날짜로 갱신
						mainService.updateSetTodayDateByViewsIdxAndViewsSessionIdx(todayMap);

						// 해당 미니홈피 유저의 조회된 기록 중 접속 날짜가 현재 날짜와 다를 경우
						if ( !miniUser.getToDate().equals(today.format(date)) ) {

							// 해당 미니홈피 유저의 일일 조회수를 누적 조회수에 추가
							miniUser.setTotal(miniUser.getTotal() + miniUser.getToday());
							// 해당 미니홈피 유저의 일일 조회수를 0으로 초기화 후 1 증가
							miniUser.setToday(1);
							// 해당 미니홈피 유저의 접속 날짜를 현재 날짜로 갱신
							miniUser.setToDate(today.format(date));
							// 수정된 값들로 해당 미니홈피 유저의 유저 정보 갱신
							signService.updateSetTodayAndTotalAndToDateByIdx(miniUser);

						// 해당 미니홈피 유저의 조회된 기록 중 접속 날짜가 현재 날짜와 같을 경우
						} else {

							// 해당 미니홈피 유저의 일일 조회수 1 증가
							miniUser.setToday(miniUser.getToday() + 1);
							// 증가된 일일 조회수로 해당 미니홈피 유저 정보 갱신
							signService.updateSetTodayByIdx(miniUser);

						}

					// 로그인한 유저의 방문 기록중 방문 날짜가 현재 날짜와 같을 경우
					} else {

						// 조회수를 증가시키지 않고 통과

					}

				// 로그인한 유저의 방문 기록이 없을 경우
				} else {

					// 로그인한 유저의 해당 미니홈피 방문 기록을 추가
					Views views = new Views();
					views.setIdx(null);
					views.setViewsIdx((Integer) todayMap.get("1"));
					views.setViewsSessionIdx((Integer) todayMap.get("2"));
					views.setTodayDate((String) todayMap.get("3"));
					mainService.insertIntoViews(views);

					// 해당 미니홈피 유저의 조회된 기록 중 접속 날짜가 현재 날짜와 다를 경우
					if ( !miniUser.getToDate().equals(today.format(date)) ) {

						// 해당 미니홈피 유저의 일일 조회수를 누적 조회수에 추가
						miniUser.setTotal(miniUser.getTotal() + miniUser.getToday());
						// 해당 미니홈피 유저의 일일 조회수를 0으로 초기화 후 1 증가
						miniUser.setToday(1);
						// 해당 미니홈피 유저의 접속 날짜를 현재 날짜로 갱신
						miniUser.setToDate(today.format(date));
						// 수정된 값들로 해당 미니홈피 유저의 유저 정보 갱신
						signService.updateSetTodayAndTotalAndToDateByIdx(miniUser);

					// 해당 미니홈피 유저의 조회된 기록 중 접속 날짜가 현재 날짜와 같을 경우
					} else {

						// 해당 미니홈피 유저의 일일 조회수 1 증가
						miniUser.setToday(miniUser.getToday() + 1);
						// 증가된 일일 조회수로 해당 미니홈피 유저 정보 갱신
						signService.updateSetTodayByIdx(miniUser);

					}

				}

			// 로그인 유저 idx와 미니홈피 유저 idx값이 같을 경우 - 내 미니홈피 조회 - 조회수 증가 X
			} else {

				// 내 미니홈피 접속 날짜 조회
				Sign myMini = signService.findByIdx(loginIdx);

				// 조회된 접속 날짜가 현재 날짜와 다를 경우
				if ( !myMini.getToDate().equals(today.format(date)) ) {

					// 내 미니홈피의 일일 조회수를 누적 조회수에 추가
					myMini.setTotal(myMini.getTotal() + myMini.getToday());
					// 내 미니홈피의 일일 조회수를 0으로 초기화
					myMini.setToday(0);
					// 내 미니홈피의 접속 날짜를 현재 날짜로 갱신
					myMini.setToDate(today.format(date));
					// 수정된 값들로 내 미니홈피 정보 갱신
					signService.updateSetTodayAndTotalAndToDateByIdx(myMini);

				// 조회된 접속 날짜가 현재 날짜와 같을 경우
				} else {

					// 조회수를 증가시키지 않고 통과

				}

			}

		}
		
		// 조회수 구역 끝 //
		
		// 그 다음 idx에 해당하는 방명록의 모든 방문글을 조회
		List<Guestbook> list = guestbookService.findByGuestbookIdxOrderByIdxDesc(idx);
		// 조회된 모든 방문글을 리스트 형태로 바인딩
		model.addAttribute("list", list);
		
		// 그 다음 idx에 해당하는 유저 정보를 조회
		Sign sign = signService.findByIdx(idx);
		// 조회된 유저 정보를 바인딩
		model.addAttribute("sign", sign);
		// 로그인 유저 idx를 바인딩
		model.addAttribute("loginIdx", loginIdx);
		
		// 그 다음 일촌 관계를 알아보기 위해 Ilchon을 생성
		Ilchon ilchon = new Ilchon();
		
		// 맞일촌 상태를 알리는 ilchonUp을 2로 지정
		ilchon.setIlchonUp(2);
		// 일촌 idx에 idx를 지정
		ilchon.setIlchonSessionIdx(loginIdx);
		// 그 다음 idx에 해당하는 일촌 조회
		List<Ilchon> ilchonList = mainService.findByIlchonSessionIdxAndIlchonUp(ilchon);
		// 조회된 맞일촌을 리스트 형태로 바인딩
		model.addAttribute("ilchonList", ilchonList);
		
		// 방명록 페이지로 이동
		return "Page/Guestbook/guestbook_list";
	}
	
	// 방명록 방문글 작성 페이지로 이동
	@RequestMapping("/guestbook_insert_form.do")
	public String guestbook_insert_form(int idx, Model model) {
		// Authorization 헤더에 토큰이 존재하는지 값체크
		String authorization = headers.getFirst("Authorization");
		// 헤더에 토큰이 존재하지 않는 경우 - 에러
		if ( authorization == null ) {
			// 세션에 값이 존재하는지 체크한다.
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
		// 헤더에 토큰이 존재하는 경우 - 정상
		// JWT의 토큰에 해당하는 idx 추출
		int loginIdx = jwtUtil.validationToken(authorization.substring("Bearer ".length()));
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 페이지로 이동
			return "Page/Guestbook/guestbook_list";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// JWT의 리프레쉬 토큰으로 토큰 재생성
			String refreshToken = jwtUtil.validationRefreshToken(authorization.substring("Bearer ".length()));
			// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
			// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
			if ( refreshToken == null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
			} else {
				// 세션에 값이 존재하는지 체크한다.
				HttpSession session = request.getSession();
				// 세션에 값이 존재하지 않는 경우 - 대기 시간 1시간 이후
				if ( session.getAttribute("login") == null ) {
					// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
					jwtUtil.timeoutToken(refreshToken);
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 방명록 페이지로 이동
					return "Page/Guestbook/guestbook_list";
				// 세션에 값이 존재하는 경우 - 대기 시간 1시간 이전
				} else {
					// Authorization 헤더에 재생성한 토큰 부여
					headers.add("Authorization", "Bearer " + refreshToken);
					// JWT의 재생성한 토큰에 해당하는 idx 추출
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);
		
		// 방명록에 작성자를 저장하기 위해 로그인 유저 idx에 해당하는 유저 정보를 조회
		Sign loginUser = signService.findByIdx(loginIdx);
		
		// 방문글 작성자 정보 생성
		Guestbook guestbook = new Guestbook();

		// 미니홈피 유저 idx 지정
		guestbook.setGuestbookIdx(idx);
		// 작성자 idx 지정
		guestbook.setGuestbookSessionIdx(loginIdx);
		// 작성자 미니미 지정
		guestbook.setGuestbookMinimi(loginUser.getMinimi());
		
		if ( loginUser.getPlatform().equals("cyworld") ) {
			// 플랫폼이 cyworld일 경우 - ID + @ + cyworld = qwer@cyworld - 폐기
			// guestbook.setGuestbookContentName(loginUser.getUserID() + "@" + loginUser.getPlatform());
			
			// 플랫폼이 cyworld일 경우 - ( + 이름 + / + ID + ) = ( 관리자 / qwer ) - 변경
			guestbook.setGuestbookName("( " + loginUser.getName() + " / " + loginUser.getUserId() + " )");
		} else {
			/* 플랫폼이 소셜일 경우 - 이메일 @부분까지 잘라낸 뒤 플랫폼명 추가 - 폐기
			 * 네이버 - qwer@ + naver = qwer@naver
			 * 카카오 - qwer@ + kakao = qwer@kakao
			 */
			// guestbook.setGuestbookContentName(loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") + 1 ) + loginUser.getPlatform());
			
			/* 플랫폼이 소셜일 경우 ID가 없으므로 이메일로 대체 - 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
			 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 */
			guestbook.setGuestbookName("( " + loginUser.getName() + " / " + loginUser.getEmail().substring( 0, loginUser.getEmail().indexOf("@") ) + " )");
		}

		// 방문글 작성자 정보 바인딩
		model.addAttribute("guestbook", guestbook);

		// 방명록 작성 페이지로 이동
		return "Page/Guestbook/guestbook_insert_form";
	}
	
	// 방명록 새 방문글 작성
	@RequestMapping("/guestbook_insert.do")
	public String guestbook_insert(Guestbook guestbook, Model model) {
		// Authorization 헤더에 토큰이 존재하는지 체크
		String authorization = headers.getFirst("Authorization");
		// 헤더에 토큰이 존재하지 않는 경우 - 에러
		if ( authorization == null ) {
			// 세션에 값이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 해당 미니홈피 유저의 메인 페이지로 이동
				return "redirect:main.do?idx=" + guestbook.getGuestbookIdx();
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 로그인 페이지로 이동
				return "redirect:login.do";
			}
		}
		// 헤더에 토큰이 존재하는 경우 - 정상
		// JWT의 토큰에 해당하는 idx 추출
		int loginIdx = jwtUtil.validationToken(authorization.substring("Bearer ".length()));
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 작성 페이지로 이동
			return "Page/Guestbook/guestbook_insert_form";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// JWT의 리프레쉬 토큰으로 토큰 재생성
			String refreshToken = jwtUtil.validationRefreshToken(authorization.substring("Bearer ".length()));
			// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
			// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
			if ( refreshToken == null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 방명록 작성 페이지로 이동
				return "Page/Guestbook/guestbook_insert_form";
			// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
			} else {
				// 세션에 값이 존재하는지 체크한다.
				HttpSession session = request.getSession();
				// 세션에 값이 존재하지 않는 경우 - 대기 시간 1시간 이후
				if ( session.getAttribute("login") == null ) {
					// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
					jwtUtil.timeoutToken(refreshToken);
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 방명록 작성 페이지로 이동
					return "Page/Guestbook/guestbook_insert_form";
				// 세션에 값이 존재하는 경우 - 대기 시간 1시간 이전
				} else {
					// Authorization 헤더에 재생성한 토큰 부여
					headers.add("Authorization", "Bearer " + refreshToken);
					// JWT의 재생성한 토큰에 해당하는 idx 추출
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 토큰에서 추출한 로그인 유저 idx와 좋아요에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 해당 미니홈피 유저의 사진첩 페이지로 이동
			return "redirect:guestbook.do?idx=" + guestbook.getGuestbookIdx();
		}

		// 작성 시간을 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 방명록 Idx에 AUTO_INCREMENT로 null 지정
		guestbook.setIdx(null);
		// 방문글에 좋아요 시작 개수 0 지정
		guestbook.setGuestbookLikeNum(0);
		// 방문글에 작성 시간 지정
		guestbook.setGuestbookRegDate(today.format(date));

		// 작성한 방문글을 저장
		guestbookService.insertIntoGuestbook(guestbook);

		// idx를 들고 방명록 페이지 URL로 이동
		return "redirect:guestbook.do?idx=" + guestbook.getGuestbookIdx();
	}
	
	// 방명록 방문글 삭제
	@RequestMapping("/guestbook_delete.do")
	@ResponseBody // Ajax로 요청된 메서드는 결과를 콜백 메서드로 돌아가기 위해 반드시 필요한 어노테이션
	public String guestbook_delete(Guestbook guestbook) {
		// Authorization 헤더에 토큰이 존재하는지 체크
		String authorization = headers.getFirst("Authorization");
		// 헤더에 토큰이 존재하지 않는 경우 - 에러
		if ( authorization == null ) {
			// 세션에 값이 존재하는지 체크한다.
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
		// 헤더에 토큰이 존재하는 경우 - 정상
		// JWT의 토큰에 해당하는 idx 추출
		int loginIdx = jwtUtil.validationToken(authorization.substring("Bearer ".length()));
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// 에러 코드를 반환한다.
			return "-99";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// JWT의 리프레쉬 토큰으로 토큰 재생성
			String refreshToken = jwtUtil.validationRefreshToken(authorization.substring("Bearer ".length()));
			// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
			// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
			if ( refreshToken == null ) {
				// 에러 코드를 반환한다.
				return "-100";
			// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
			} else {
				// 세션에 값이 존재하는지 체크한다.
				HttpSession session = request.getSession();
				// 세션에 값이 존재하지 않는 경우 - 대기 시간 1시간 이후
				if ( session.getAttribute("login") == null ) {
					// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
					jwtUtil.timeoutToken(refreshToken);
					// 에러 코드를 반환한다.
					return "-1";
				// 세션에 값이 존재하는 경우 - 대기 시간 1시간 이전
				} else {
					// Authorization 헤더에 재생성한 토큰 부여
					headers.add("Authorization", "Bearer " + refreshToken);
					// JWT의 재생성한 토큰에 해당하는 idx 추출
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다른 경우 - 방문글은 미니홈피 유저 혹은 작성자만 삭제할 수 있다.
		if ( loginIdx != guestbook.getGuestbookIdx() && loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 삭제 실패할 경우
		String result = "no";
		
		// DB에 저장된 방문글 중 가져온 정보에 해당하는 방문글 삭제
		int res = guestbookService.deleteByGuestbookIdxAndIdx(guestbook);
		if (res == 1) {
			// 삭제 성공할 경우
			result = "yes";
		}

		// 콜백 메소드에 전달
		return result;
	}
	
	// 방명록 방문글 수정 페이지로 이동
	@RequestMapping("/guestbook_modify_form.do")
	public String guestbook_modify_form(Guestbook guestbook, Model model) {
		// Authorization 헤더에 토큰이 존재하는지 체크
		String authorization = headers.getFirst("Authorization");
		// 헤더에 토큰이 존재하지 않는 경우 - 에러
		if ( authorization == null ) {
			// 세션에 값이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 해당 미니홈피 유저의 메인 페이지로 이동
				return "redirect:main.do?idx=" + guestbook.getGuestbookIdx();
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 로그인 페이지로 이동
				return "redirect:login.do";
			}
		}
		// 헤더에 토큰이 존재하는 경우 - 정상
		// JWT의 토큰에 해당하는 idx 추출
		int loginIdx = jwtUtil.validationToken(authorization.substring("Bearer ".length()));
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 방명록 페이지로 이동
			return "Page/Guestbook/guestbook_list";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// JWT의 리프레쉬 토큰으로 토큰 재생성
			String refreshToken = jwtUtil.validationRefreshToken(authorization.substring("Bearer ".length()));
			// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
			// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
			if ( refreshToken == null ) {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
				// 방명록 페이지로 이동
				return "Page/Guestbook/guestbook_list";
			// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
			} else {
				// 세션에 값이 존재하는지 체크한다.
				HttpSession session = request.getSession();
				// 세션에 값이 존재하지 않는 경우 - 대기 시간 1시간 이후
				if ( session.getAttribute("login") == null ) {
					// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
					jwtUtil.timeoutToken(refreshToken);
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "세션이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 방명록 페이지로 이동
					return "Page/Guestbook/guestbook_list";
				// 세션에 값이 존재하는 경우 - 대기 시간 1시간 이전
				} else {
					// Authorization 헤더에 재생성한 토큰 부여
					headers.add("Authorization", "Bearer " + refreshToken);
					// JWT의 재생성한 토큰에 해당하는 idx 추출
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}
		// 에러 메시지에 정상이라는 의미로 null을 바인딩한다.
		model.addAttribute("errMsg", null);

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다른 경우 - 방문글은 오로지 작성자만 수정할 수 있다.
		if ( loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 해당 미니홈피 유저의 방명록 페이지로 이동
			return "redirect:guestbook.do?idx=" + guestbook.getGuestbookIdx();
		}
		
		// 방명록에 작성자를 저장하기 위해 로그인 유저 idx에 해당하는 유저 정보를 조회
		Sign loginUser = signService.findByIdx(loginIdx);

		// 해당 idx의 방명록에 수정할 방문글을 조회
		Guestbook updateGuestbook = guestbookService.findByGuestbookIdxAndIdx(guestbook);
		if ( updateGuestbook != null ) {
			// 작성자 미니미 지정
			updateGuestbook.setGuestbookMinimi(loginUser.getMinimi());
			// 조회된 방문글을 바인딩
			model.addAttribute("updateGuestbook", updateGuestbook);
		}

		// 수정 페이지로 이동
		return "Page/Guestbook/guestbook_modify_form";
	}
	
	// 방문글 수정하기
	@RequestMapping("/guestbook_modify.do")
	@ResponseBody
	public String guestbook_modify(Guestbook guestbook) {
		// Authorization 헤더에 토큰이 존재하는지 체크
		String authorization = headers.getFirst("Authorization");
		// 헤더에 토큰이 존재하지 않는 경우 - 에러
		if ( authorization == null ) {
			// 세션에 값이 존재하는지 체크한다.
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
		// 헤더에 토큰이 존재하는 경우 - 정상
		// JWT의 토큰에 해당하는 idx 추출
		int loginIdx = jwtUtil.validationToken(authorization.substring("Bearer ".length()));
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// 에러 코드를 반환한다.
			return "-99";
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// JWT의 리프레쉬 토큰으로 토큰 재생성
			String refreshToken = jwtUtil.validationRefreshToken(authorization.substring("Bearer ".length()));
			// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
			// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
			if ( refreshToken == null ) {
				// 에러 코드를 반환한다.
				return "-100";
			// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
			} else {
				// 세션에 값이 존재하는지 체크한다.
				HttpSession session = request.getSession();
				// 세션에 값이 존재하지 않는 경우 - 대기 시간 1시간 이후
				if ( session.getAttribute("login") == null ) {
					// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
					jwtUtil.timeoutToken(refreshToken);
					// 에러 코드를 반환한다.
					return "-1";
				// 세션에 값이 존재하는 경우 - 대기 시간 1시간 이전
				} else {
					// Authorization 헤더에 재생성한 토큰 부여
					headers.add("Authorization", "Bearer " + refreshToken);
					// JWT의 재생성한 토큰에 해당하는 idx 추출
					loginIdx = jwtUtil.validationToken(refreshToken);
				}
			}
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다른 경우 - 방문글은 오로지 작성자만 수정할 수 있다.
		if ( loginIdx != guestbook.getGuestbookSessionIdx() ) {
			// 에러 코드를 반환한다.
			return "-4";
		}

		// 갱신 실패할 경우
		String result = "no";

		// 수정 시간을 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 방문글에 수정 시간 지정
		guestbook.setGuestbookRegDate(today.format(date));
		
		// 수정된 방문글로 갱신
		int res = guestbookService.updateSetGuestbookNameAndGuestbookMinimiAndGuestbookRegDateAndGuestbookContentAndGuestbookSecretCheckByGuestbookIdxAndGuestbookSessionIdxAndIdx(guestbook);
		if (res != 0) {
			// 갱신 성공할 경우
			result = "yes";
		}

		// 콜백 메소드에 전달
		return result;
	}
	
	/////////////// 방명록 좋아요 구역 ///////////////
	
	@RequestMapping("/guestbook_like.do")
	@ResponseBody
	public Guestbook guestbook_like(Guestbook guestbook, GuestbookLike guestbookLike) {
		// Authorization 헤더에 토큰이 존재하는지 체크
		String authorization = headers.getFirst("Authorization");
		// 헤더에 토큰이 존재하지 않는 경우 - 에러
		if ( authorization == null ) {
			// 세션에 값이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 토큰은 존재하지 않지만 세션은 존재하는 경우 - 비회원
			if ( session.getAttribute("login") != null ) {
				// 방문글 내용에 에러 코드 지정
				guestbook.setGuestbookContent("-4");
				// 갱신된 방문글 전달
				return guestbook;
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 방문글 내용에 에러 코드 지정
				guestbook.setGuestbookContent("0");
				// 갱신된 방문글 전달
				return guestbook;
			}
		}
		// 헤더에 토큰이 존재하는 경우 - 정상
		// JWT의 토큰에 해당하는 idx 추출
		int loginIdx = jwtUtil.validationToken(authorization.substring("Bearer ".length()));
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// 방문글 내용에 에러 코드 지정
			guestbook.setGuestbookContent("-99");
			// 갱신된 방문글 전달
			return guestbook;
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// Authorization 헤더 제거
			headers.remove("Authorization");
			// JWT의 리프레쉬 토큰으로 토큰 재생성
			String refreshToken = jwtUtil.validationRefreshToken(authorization.substring("Bearer ".length()));
			// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
			// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
			if ( refreshToken == null ) {
				// 방문글 내용에 에러 코드 지정
				guestbook.setGuestbookContent("-100");
				// 갱신된 방문글 전달
				return guestbook;
			// 토큰이 재생성된 경우 - 리프레쉬 토큰 유지
			} else {
				// 세션에 값이 존재하는지 체크한다.
				HttpSession session = request.getSession();
				// 세션에 값이 존재하지 않는 경우 - 대기 시간 1시간 이후
				if ( session.getAttribute("login") == null ) {
					// 재생성한 토큰과 리프레쉬 토큰을 삭제한다.
					jwtUtil.timeoutToken(refreshToken);
					// 방문글 내용에 에러 코드 지정
					guestbook.setGuestbookContent("-1");
					// 갱신된 방문글 전달
					return guestbook;
				// 세션에 값이 존재하는 경우 - 대기 시간 1시간 이전
				} else {
					// Authorization 헤더에 재생성한 토큰 부여
					headers.add("Authorization", "Bearer " + refreshToken);
					// JWT의 재생성한 토큰에 해당하는 idx 추출
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

		// 미니홈피 유저 idx 지정
		guestbook.setGuestbookIdx(guestbookLike.getGuestbookLikeIdx());
		// 좋아요를 누른 방문글의 번호 지정
		guestbook.setIdx(guestbookLike.getGuestbookLikeRef());

		// 먼저 DB에 로그인한 유저가 해당 idx의 방명록에 남긴 방문글에 좋아요를 눌렀는지 조회
		GuestbookLike guestbookLikeCheck = guestbookService.findByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(guestbookLike);

		// 이미 좋아요를 눌렀을 경우
		if ( guestbookLikeCheck != null ) {
			// 이미 눌린 좋아요를 다시 누를 경우 취소되므로 좋아요 내역을 삭제
			guestbookService.deleteByGuestbookLikeIdxAndGuestbookLikeRefAndGuestbookLikeSessionIdx(guestbookLike);
			// 좋아요 개수 조회
			int likeCount = guestbookService.countByGuestbookLikeIdxAndGuestbookLikeRef(guestbookLike);
			// 조회된 좋아요 개수를 지정
			guestbook.setGuestbookLikeNum(likeCount);
			// 조회된 좋아요 개수로 갱신
			guestbookService.updateSetGuestbookLikeNumByGuestbookIdxAndIdx(guestbook);
			// 콜백 메소드에 갱신된 방문글 전달
			return guestbook;
		// 좋아요를 안 눌렀을 경우
		} else {
			// 좋아요 Idx에 AUTO_INCREMENT로 null 지정
			guestbookLike.setIdx(null);
			// 좋아요를 누를 경우 좋아요 내역을 추가
			guestbookService.insertIntoGuestbookLike(guestbookLike);
			// 좋아요 개수 조회
			int likeCount = guestbookService.countByGuestbookLikeIdxAndGuestbookLikeRef(guestbookLike);
			// 조회된 좋아요 개수를 지정
			guestbook.setGuestbookLikeNum(likeCount);
			// 조회된 좋아요 개수로 갱신
			guestbookService.updateSetGuestbookLikeNumByGuestbookIdxAndIdx(guestbook);
			// 콜백 메소드에 갱신된 방문글 전달
			return guestbook;
		}
	}
}