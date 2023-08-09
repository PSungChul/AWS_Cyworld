package com.social.cyworld.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.social.cyworld.entity.Diary;
import com.social.cyworld.entity.Sign;
import com.social.cyworld.service.DiaryService;
import com.social.cyworld.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DiaryController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	SignService signService;
	@Autowired
	DiaryService diaryService;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 다이어리 조회
	@RequestMapping("/diary.do")
	public String list(Integer idx,Model model) {
		// 다이어리에 들어오면 가장 먼저 세션값이 있는지 확인
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
		
		// 그 다음 idx에 해당하는 다이어리의 모든 글을 조회
		List<Diary> list = diaryService.findByDiaryIdxOrderByIdxDesc(idx);
		// 조회된 모든 다이어리 글을 리스트 형태로 바인딩
		model.addAttribute("list", list);
		
		// 그 다음 idx에 해당하는 유저 정보를 조회
		Sign sign = signService.findByIdx(idx);
		// 조회된 유저 정보를 바인딩
		model.addAttribute("sign", sign);
		// 추가로 세션값도 바인딩
		model.addAttribute("sessionIdx", session.getAttribute("login"));
		
		// 다이어리 페이지로 이동
		return "Page/Diary/diary_list";
	}
	
	// 다이어리 글 작성 페이지로 이동
	@RequestMapping("/diary_insert_form.do")
	public String insert_form(int idx) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 다이어리는 오로지 미니홈피 주인만 작성할 수 있다.
		if ( (Integer) session.getAttribute("login") != idx ) {
			// 해당 미니홈피 유저의 다이어리 페이지로 이동
			return "redirect:diary.do?idx=" + idx;
		}
		
		// 세션값이 있다면 작성 페이지로 이동
		return "Page/Diary/diary_insert_form";
	}

	// 다이어리 새 글 작성
	@RequestMapping("/diary_insert.do")
	public String insert(int diaryIdx, Diary diary) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 다이어리는 오로지 미니홈피 주인만 작성할 수 있다.
		if ( (Integer) session.getAttribute("login") != diaryIdx ) {
			// 해당 미니홈피 유저의 다이어리 페이지로 이동
			return "redirect:diary.do?idx=" + diaryIdx;
		}

		// 작성 시간를 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 다이어리 Idx에 AUTO_INCREMENT로 null 지정
		diary.setIdx(null);
		// 다이어리에 작성 시간 지정
		diary.setDiaryRegDate(today.format(date));

		// 작성한 다이어리 글을 저장
		diaryService.insertIntoDiary(diary);

		// idx를 들고 다이어리 페이지 URL로 이동
		return "redirect:diary.do?idx=" + diary.getDiaryIdx();
	}
	
	// 다이어리 글 삭제
	@RequestMapping("/diary_delete.do")
	@ResponseBody
	public String delete(int diaryIdx, Diary diary) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인페이지로 이동
			return "redirect:login.do";
		}

		// 삭제 실패할 경우
		String result = "no";

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 다이어리는 오로지 미니홈피 주인만 삭제할 수 있다.
		if ( (Integer) session.getAttribute("login") != diaryIdx ) {
			// 콜백 메소드에 전달
			return "redirect:diary.do?idx=" + diaryIdx;
		}
		
		// DB에 저장된 다이어리 글 중 가져온 정보에 해당하는 다이어리 글 삭제
		int res = diaryService.deleteByDiaryIdxAndIdx(diary);
		
		if (res == 1) {
			// 삭제 성공할 경우
			result = "yes";
		}
		
		// 콜백 메소드에 전달
		return result;
	}
	
	// 다이어리 글 수정 페이지로 이동
	@RequestMapping("/diary_modify_form.do")
	public String modify_form(int diaryIdx, Diary diary, Model model) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 다이어리는 오로지 미니홈피 주인만 수정할 수 있다.
		if ( (Integer) session.getAttribute("login") != diaryIdx ) {
			// 해당 미니홈피 유저의 다이어리 페이지로 이동
			return "redirect:diary.do?idx=" + diaryIdx;
		}
		
		// 해당 idx의 다이어리에 수정할 글을 조회
		Diary updateDiary = diaryService.findByDiaryIdxAndIdx(diary);
		if (updateDiary != null) {
			// 조회된 다이어리 글을 바인딩
			model.addAttribute("updateDiary", updateDiary);
		}
		
		// 수정 페이지로 이동
		return "Page/Diary/diary_modify_form";
	}
	
	// 다이어리 글 수정하기
	@RequestMapping("/diary_modify.do")
	@ResponseBody
	public String modify(int diaryIdx, Diary diary) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 갱신 실패할 경우 - JSON형태
		String result = "{'result':'no'}";

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 다이어리는 오로지 미니홈피 주인만 수정할 수 있다.
		if ( (Integer) session.getAttribute("login") != diaryIdx ) {
			// 콜백 메소드에 전달
			return result;
		}

		// 수정 시간을 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 다이어리에 수정 시간 지정
		diary.setDiaryRegDate(today.format(date));
		
		// 수정된 다이어리 글로 갱신
		int res = diaryService.updateSetDiaryContentAndDiaryRegDateByDiaryIdxAndIdx(diary);

		if (res != 0) {
			// 갱신 성공할 경우 - JSON형태
			result = "{'result':'yes'}";
		}
		
		// 콜백 메소드에 전달
		return result;
	}
}