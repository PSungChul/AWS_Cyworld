package com.social.cyworld.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.social.cyworld.entity.*;
import com.social.cyworld.service.GuestbookService;
import com.social.cyworld.service.MainService;
import com.social.cyworld.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
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
	SignService signService;
	@Autowired
	MainService mainService;
	@Autowired
	GuestbookService guestbookService;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 방명록 조회
	@RequestMapping("/guestbook.do")
	public String guestbook_list (Integer idx, Model model) {
		// 방명록에 들어오면 가장 먼저 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// 비회원이 접근할 경우
		if ( (Integer) session.getAttribute("login") < 0 ) {
			// 해당 미니홈피 유저의 메인 페이지로 이동
			return "redirect:main.do?idx=" + idx;
		}
		
		// 조회수 구역 시작 //
		
		// 먼저 접속 날짜를 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 그냥 사용하면 뒤에 시간까지 모두 기록되기에 날짜만 따로 뺴는 작업을 한다
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
		
		// 그리고 앞으로 사용할 로그인한 유저의 idx와 해당 미니홈피의 idx와 접속 날짜를 편하게 사용하기 위해 Map으로 만들어 둔다
		HashMap<String, Object> todayMap = new HashMap<String, Object>();
		todayMap.put("1", idx); // 로그인한 유저의 idx
		todayMap.put("2", session.getAttribute("login")); // 해당 미니홈피 유저의 idx
		todayMap.put("3", today.format(date)); // 접속 날짜

		// 세션값이 비회원이 아닐 경우 - 세션값이 비회원일 경우 조회수 증가 X
		if ( (Integer) session.getAttribute("login") > 0 ) {

			// 세션값과 idx값이 다를 경우 - 타 유저 미니홈피 조회 - 조회수 증가 O
			if ( ( (Integer) session.getAttribute("login") != idx ) ) {

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

			// 세션값과 idx값이 같을 경우 - 내 미니홈피 조회 - 조회수 증가 X
			} else {

				// 내 미니홈피 접속 날짜 조회
				Sign myMini = signService.findByIdx((Integer) session.getAttribute("login"));

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
		// 추가로 세션값도 바인딩
		model.addAttribute("sessionIdx", session.getAttribute("login"));
		
		// 그 다음 일촌 관계를 알아보기 위해 IlchonVO를 생성
		Ilchon ilchon = new Ilchon();
		
		// 맞일촌 상태를 알리는 ilchonUp을 2로 지정
		ilchon.setIlchonUp(2);
		// 일촌 idx에 idx를 지정
		ilchon.setIlchonSessionIdx((Integer) session.getAttribute("login"));
		// 그 다음 idx에 해당하는 일촌 조회
		List<Ilchon> ilchonList = mainService.findByIlchonSessionIdxAndIlchonUp(ilchon);
		// 조회된 맞일촌을 리스트 형태로 바인딩
		model.addAttribute("ilchonList", ilchonList);
		
		// 방명록 페이지로 이동
		return "Page/Guestbook/guestbook_list";
	}
	
	// 방명록 방문글 작성 페이지로 이동
	@RequestMapping("/guestbook_insert_form.do")
	public String guestbook_insert_form(Model model) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// 방명록에 작성자를 저장하기 위한 세션값에 해당하는 유저 정보를 조회
		Sign sessionUser = signService.findByIdx((Integer) session.getAttribute("login"));
		
		// 방문글 작성자 정보 지정
		Guestbook guestbook = new Guestbook();
		
		if ( sessionUser.getPlatform().equals("cyworld") ) {
			// 플랫폼이 cyworld일 경우 - ID + @ + cyworld = qwer@cyworld - 폐기
			// vo.setGuestbookContentName(sessionUser.getUserID() + "@" + sessionUser.getPlatform());
			
			// 플랫폼이 cyworld일 경우 - ( + 이름 + / + ID + ) = ( 관리자 / qwer ) - 변경
			guestbook.setGuestbookName("( " + sessionUser.getName() + " / " + sessionUser.getUserId() + " )");
		} else {
			/* 플랫폼이 소셜일 경우 - 이메일 @부분까지 잘라낸 뒤 플랫폼명 추가 - 폐기
			 * 네이버 - qwer@ + naver = qwer@naver
			 * 카카오 - qwer@ + kakao = qwer@kakao
			 */
			// vo.setGuestbookContentName(sessionUser.getEmail().substring( 0, sessionUser.getEmail().indexOf("@") + 1 ) + sessionUser.getPlatform());
			
			/* 플랫폼이 소셜일 경우 ID가 없으므로 이메일로 대체 - 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
			 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 */
			guestbook.setGuestbookName("( " + sessionUser.getName() + " / " + sessionUser.getEmail().substring( 0, sessionUser.getEmail().indexOf("@") ) + " )");
		}
		// 미리 지정한 작성자 이를을 바인딩
		model.addAttribute("guestbookName", guestbook.getGuestbookName());
		// 세션값으로 미니미 지정
		model.addAttribute("minimi", sessionUser.getMinimi());
		// 작성 페이지로 이동
		return "Page/Guestbook/guestbook_insert_form";
	}
	
	// 방명록 새 방문글 작성
	@RequestMapping("/guestbook_insert.do")
	public String guestbook_insert(Guestbook guestbook) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// 방문글에 작성자의 idx를 저장하기 위해 Integer로 형변환
		Integer sessionIdx = (Integer) session.getAttribute("login");
		// 작성 시간을 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 방명록 Idx에 AUTO_INCREMENT로 null 지정
		guestbook.setIdx(null);
		// 방문글에 좋아요 시작 개수 0 지정
		guestbook.setGuestbookLikeNum(0);
		// 방문글에 작성자 idx 지정
		guestbook.setGuestbookSessionIdx(sessionIdx);
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
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 삭제 실패할 경우
		String result = "no";

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 방문글은 미니홈피 주인 혹은 작성자만 삭제할 수 있다.
		if ( (Integer) session.getAttribute("login") != guestbook.getGuestbookIdx() && (Integer) session.getAttribute("login") != guestbook.getGuestbookSessionIdx() ) {
			System.out.println(1);
			// 콜백 메소드에 전달
			return result ;
		}

		System.out.println(2);
		
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
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 방문글은 오로지 작성자만 수정할 수 있다.
		if ( (Integer) session.getAttribute("login") != guestbook.getGuestbookSessionIdx() ) {
			// 해당 미니홈피 유저의 방명록 페이지로 이동
			return "redirect:guestbook.do?idx=" + guestbook.getGuestbookIdx() ;
		}
		
		// 방명록에 작성자를 저장하기 위한 세션값에 해당하는 유저 정보를 조회
		Sign sessionUser = signService.findByIdx((Integer) session.getAttribute("login"));

		// 해당 idx의 방명록에 수정할 방문글을 조회
		Guestbook updateGuestbook = guestbookService.findByGuestbookIdxAndIdx(guestbook);
		if ( updateGuestbook != null ) {
			// 조회된 방문글을 바인딩
			model.addAttribute("updateGuestbook", updateGuestbook);
			// 세션값으로 미니미 지정
			model.addAttribute("minimi", sessionUser.getMinimi());
		}

		// 수정 페이지로 이동
		return "Page/Guestbook/guestbook_modify_form";
	}
	
	// 방문글 수정하기
	@RequestMapping("/guestbook_modify.do")
	@ResponseBody
	public String guestbook_modify(Guestbook guestbook) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 갱신 실패할 경우 - JSON형태
		String result = "{'result':'no'}";

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 방문글은 오로지 작성자만 수정할 수 있다.
		if ( (Integer) session.getAttribute("login") != guestbook.getGuestbookSessionIdx() ) {
			// 콜백 메소드에 전달
			return result;
		}

		// 수정 시간을 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 방문글에 수정 시간 지정
		guestbook.setGuestbookRegDate(today.format(date));
		
		// 수정된 방문글로 갱신
		int res = guestbookService.updateSetGuestbookNameAndGuestbookMinimiAndGuestbookRegDateAndGuestbookContentAndGuestbookSecretCheckByGuestbookIdxAndGuestbookSessionIdxAndIdx(guestbook);

		if (res != 0) {
			// 갱신 성공할 경우 - JSON형태
			result = "{'result':'yes'}";
		}

		// 콜백 메소드에 전달
		return result;
	}
	
	/////////////// 방명록 좋아요 구역 ///////////////
	
	@RequestMapping("/guestbook_like.do")
	@ResponseBody // 콜백 메소드에 VO를 전달
	public Guestbook geustbook_like(Guestbook guestbook, GuestbookLike guestbookLike) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 콜백메소드에 null을 전달
			return null;
		}
		
		// 세션값을 사용하기 위해 Integer타입으로 형변환
		Integer sessionIdx = (Integer) session.getAttribute("login");

		// 좋아요를 누른 로그인한 유저의 세션값 지정
		guestbookLike.setGuestbookLikeSessionIdx(sessionIdx);
		// 방명록의 idx 지정
		guestbookLike.setGuestbookLikeIdx(guestbook.getGuestbookIdx());
		// 좋아요를 누른 방문글의 번호
		guestbookLike.setGuestbookLikeRef(guestbook.getIdx());

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
			// 콜백 메소드에 VO를 전달
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
			// 콜백 메소드에 VO를 전달
			return guestbook;
		}
	}
}