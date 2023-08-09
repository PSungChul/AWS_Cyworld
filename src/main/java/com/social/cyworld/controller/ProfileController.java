package com.social.cyworld.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.social.cyworld.dto.LeftProfileDTO;
import com.social.cyworld.entity.BuyMinimi;
import com.social.cyworld.entity.Ilchon;
import com.social.cyworld.entity.Sign;
import com.social.cyworld.service.MainService;
import com.social.cyworld.service.ProfileService;
import com.social.cyworld.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProfileController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	SignService signService;
	@Autowired
	MainService mainService;
	@Autowired
	ProfileService profileService;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 프로필 조회
	@RequestMapping("/profile.do")
	public String profile(int idx, Model model) {
		// 프로필에 들어오면 가장 먼저 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 프로필 변경창은 오로지 미니홈피 주인만 들어갈 수 있다
		if ( (Integer) session.getAttribute("login") != idx ) {
			// 해당 미니홈피 유저의 메인 페이지로 이동
			return "redirect:main.do?idx=" + idx;
		}
		
		// 그 다음 idx에 해당하는 프로필 조회
		Sign sign = signService.findByIdx(idx);
		// 조회된 프로필 정보를 바인딩
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

		// 좌측 프로필 DTO에 좌측 프로필 정보 전달
		LeftProfileDTO leftProfileDTO = new LeftProfileDTO(sign);
		// 좌측 프로필 DTO 바인딩
		model.addAttribute("leftProfileDTO", leftProfileDTO);

		// 프로필 페이지로 이동
		return "Page/profile";
	}
	
	// 미니미 수정 팝업 페이지 이동
	@RequestMapping("/profile_minimi_popup.do")
	public String popup(Integer idx, Model model) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// idx에 해당하는 프로필 조회
		Sign sign = signService.findByIdx(idx);
		// 조회된 프로필 정보중 미니미 정보를 바인딩
		model.addAttribute("minimi", sign.getMinimi());
		// 조회된 프로필 정보중 도토리 개수를 바인딩
		model.addAttribute("dotory", sign.getDotory());
		
		// idx에 해당하는 구매한 미니미 조회
		List<BuyMinimi> buyMinimiList = profileService.findByBuyIdx(idx);
		// 조회된 구매한 미니미를 리스트 형태로 바인딩
		model.addAttribute("buyMinimi", buyMinimiList);
		
		// 미니미 팝업 페이지로 이동
		return "Page/minimiPopUp";
	}
	
	// 미니미 변경
	@RequestMapping("/profile_minimi_change.do")
	public String minimiChange(Sign sign) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// 변경할 미니미 정보로 갱신
		profileService.updateSetMinimiByIdx(sign);
		// idx를 들고 미니미 팝업 페이지 URL로 이동
		return "redirect:profile_minimi_popup.do?idx=" + sign.getIdx();
	}
	
	// 미니미 구매
	@RequestMapping("/profile_minimi_buy.do")
	@ResponseBody
	public String minimiBuy(Sign sign, BuyMinimi buyMinimi) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 미니미 Idx에 AUTO_INCREMENT로 null 지정
		buyMinimi.setIdx(null);
		
		// 미니미를 구매한 idx를 지정
		buyMinimi.setBuyIdx(sign.getIdx());
		
		// 이미 구매한 미니미인지 조회 - 중복 구매 방지
		BuyMinimi boughtMinimi = profileService.findByBuyIdxAndBuyMinimiName(buyMinimi);
		
		// 이미 구매한 미니미일 경우
		if ( boughtMinimi != null ) {
			return "no";
		// 아직 구매하지 않은 미니미일 경우
		} else {
			// 미니미를 구매하고 줄어든 도토리 보유 개수 갱신
			signService.updateSetDotoryByIdx(sign);
			
			// 구매한 미니미를 저장
			profileService.insertIntoBuyMinimi(buyMinimi);
			
			return "yes";
		}
	}
	
	// 프로필 좌측 - 메인 사진 및 메인 소개글 수정
	@RequestMapping("/profile_modify_main.do")
	public String profileModifyMain(LeftProfileDTO leftProfileDTO) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인페이지로 이동
			return "redirect:login.do";
		}
		
		// 메인 사진 업로드를 위해 절대 경로를 생성
		String savePath = "/Users/p._.sc/IT/Project/CyworldProject/util/files/profile"; // 절대 경로
		// 메인 사진 업로드를 위해 파라미터로 넘어온 사진의 정보
		MultipartFile mainPhotoFile = leftProfileDTO.getMainPhotoFile();
		// 업로드된 사진이 없을 경우 이미 저장되어 있는 사진 이름 지정
		String mainPhoto = leftProfileDTO.getMainPhoto();
		
		// 업로드된 사진이 있을 경우
		if ( !mainPhotoFile.isEmpty() ) {
//			// 이미 저장되어 있는 사진 삭제
//			File delFile = new File(savePath, mainPhoto);
//			delFile.delete();
			// 업로드된 사진의 원본 이름을 지정
			mainPhoto = mainPhotoFile.getOriginalFilename();
			/* 확장자 구하기
			 * 파일 원본 이름에서 마지막 .이 들어간 위치에서 한 칸 더한 위치부터 끝까지 잘라내기
			 * ex) 19292930388시바견.img --> .img --> img
			 */
			String extension = mainPhoto.substring( mainPhoto.lastIndexOf( "." ) + 1 );
			// 사진 이름 중복 및 한글 깨짐 방지를 위해 사진 이름을 시간으로 변경
			long time = System.currentTimeMillis();
			mainPhoto = String.format("%d.%s", time, extension);
			// 사진 저장할 경로를 지정
			File saveFile = new File(savePath, mainPhoto);
			// 저장할 경로가 없을 경우
			if( !saveFile.exists() ) {
				// 경로를 생성
				saveFile.mkdirs();
			}
			// 업로드된 사진을 실제로 저장 - try~catch 필요
			try {
				// 업로드된 사진을 실제로 저장해 주는 메소드
				mainPhotoFile.transferTo(saveFile);
				// 사진 이름 지정
				leftProfileDTO.setMainPhoto(mainPhoto);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 업로드된 사진 정보로 갱신
		profileService.updateSetMainPhotoAndMainTextByIdx(leftProfileDTO.toEntity());
		// idx를 들고 프로필 페이지 URL로 이동
		return "redirect:profile.do?idx=" + leftProfileDTO.getIdx();
	}
	
	// 프로필 우측 - 메인 타이틀 및 비밀번호 수정
	@RequestMapping("/profile_modify_userdata.do")
	@ResponseBody
	public String profileModifyUserData(Sign sign) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// 수정 실패할 경우
		String result = "no";
		
		/* 플랫폼이 cyworld일때와 소셜일 때 구분
		 * cyworld 가입자는 아이디와 비밀번호를 작성해서 가입했기에 비밀번호 변경이 있다
		 * 소셜 가입자는 아이디와 비밀번호를 소셜 플랫폼 것을 가져다 쓰기에 비밀번호 변경이 없다
		 */

		// 수정할 비밀번호가 있을 경우
		if ( sign.getInfo() == null ) {
			// 비밀번호 X, 메인 타이틀만 수정
			int res = profileService.updateSetMainTitleByIdx(sign);
			if ( res == 1 ) {
				// 수정 성공할 경우
				result = "yes";
			}
			// 콜백 메소드에 전달
			return result;
		// 수정할 비밀번호가 없을 경우
		} else {
			// 새로운 비밀번호 암호화
			sign.setInfo(passwordEncoder.encode(sign.getInfo()));
			// 비밀번호 및 메인 타이틀 수정
			int res = profileService.updateSetInfoAndMainTitleByIdx(sign);
			if (res == 1) {
				// 수정 성공할 경우
				result = "yes";
			}
			// 콜백 메소드에 전달
			return result;
		}
	}

	// 휴대폰 번호 변경
	@RequestMapping("/phone_update.do")
	@ResponseBody
	public String phoneUpdate(Sign sign) {
		// 수정 실패할 경우
		String result = "no";
		// 휴대폰 번호만 수정
		int res = profileService.updateSetPhoneNumberByIdx(sign);
		if ( res == 1 ) {
			// 수정 성공할 경우
			result = "yes";
		}
		// 콜백 메소드에 전달
		return result;
	}
}