package com.social.cyworld.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.social.cyworld.dto.GalleryDTO;
import com.social.cyworld.entity.Gallery;
import com.social.cyworld.entity.GalleryComment;
import com.social.cyworld.entity.GalleryLike;
import com.social.cyworld.entity.Sign;
import com.social.cyworld.service.GalleryService;
import com.social.cyworld.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class GalleryController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	SignService signService;
	@Autowired
	GalleryService galleryService;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 사진첩 조회
	@RequestMapping("/gallery.do")
	public String gallery(Integer idx, Model model) {
		// 사진첩에 들어오면 가장 먼저 세션값이 있는지 확인
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
		// 조회된 유저 정보를 바인딩
		model.addAttribute("sign", sign);
		// 추가로 세션값도 바인딩
		model.addAttribute("sessionIdx", session.getAttribute("login"));
		
		// 그 다음 사진첩에 댓글 작성자를 만들기 위해 세션값에 해당하는 유저 정보를 조회
		Sign sessionUser = signService.findByIdx((Integer) session.getAttribute("login"));
		
		// 조회한 유저정보에서 댓글 작성자를 만들어 담을 String변수
		String galleryCommentName = "";
		
		if ( sessionUser.getPlatform().equals("cyworld") ) {
			// 플랫폼이 cyworld일 경우 - ID + @ + cyworld = qwer@cyworld
			// galleryCommentName = (sessionUser.getUserID() + "@" + sessionUser.getPlatform());
			
			// 플랫폼이 cyworld일 경우 - ( + 이름 + / + ID + ) = ( 관리자 / qwer ) - 변경
			galleryCommentName = ( "( " + sessionUser.getName() + " / " + sessionUser.getUserId() + " )" );
		} else {
			/* 플랫폼이 소셜일 경우 - 이메일 @부분까지 잘라낸 뒤 플랫폼명 추가 - 폐기
			 * 네이버 - qwer@ + naver = qwer@naver
			 * 카카오 - qwer@ + kakao = qwer@kakao
			 */
			// galleryCommentName = (sessionUser.getEmail().substring( 0, sessionUser.getEmail().indexOf("@") + 1 ) + sessionUser.getPlatform());
			
			/* 플랫폼이 소셜일 경우 ID가 없으므로 이메일로 대체 - 이름 + 이메일 @부분부터 뒤쪽을 다 잘라낸다 - 변경
			 * 네이버 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 * 카카오 - ( + 관리자 + / + sksh0000 + ) = ( 관리자 / sksh0000 )
			 */
			galleryCommentName = ( "( " + sessionUser.getName() + " / " + sessionUser.getEmail().substring( 0, sessionUser.getEmail().indexOf("@") ) + " )" );
		}
		// 만들어진 댓글 작성자를 바인딩
		model.addAttribute("sessionName", galleryCommentName);
		
		// 사진첩 페이지로 이동
		return "Page/Gallery/gallery_list";
	}
	
	// 사진첩 글 작성 페이지로 이동
	@RequestMapping("/gallery_insert_form.do")
	public String gallery_insert_form(int idx, Model model) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 게시글은 오로지 미니홈피 주인만 작성할 수 있다.
		if ( (Integer) session.getAttribute("login") != idx ) {
			// 해당 미니홈피 유저의 사진첩 페이지로 이동
			return "redirect:gallery.do?idx=" + idx;
		}

		// 게시글 DTO 바인딩
		model.addAttribute("galleryDTO", new GalleryDTO());
		
		// 세션값이 있다면 작성 페이지로 이동
		return "Page/Gallery/gallery_insert_form";
	}
	
	// 사진첩 새 글 작성
	@RequestMapping("/gallery_insert.do")
	public String insert(int idx, GalleryDTO galleryDTO) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 게시글은 오로지 미니홈피 주인만 작성할 수 있다.
		if ( (Integer) session.getAttribute("login") != idx ) {
			// 해당 미니홈피 유저의 사진첩 페이지로 이동
			return "redirect:gallery.do?idx=" + idx;
		}
		
		// 클라이언트의 파일 업로드를 위해 절대 경로를 생성
		String savePath = "/Users/p._.sc/IT/Project/CyworldProject/util/files/gallery"; // 절대 경로
		// 업로드를 위해 파라미터로 넘어온 파일의 정보
		MultipartFile galleryFile = galleryDTO.getGalleryFile();
		// 업로드된 파일이 없을 경우 파일 이름 지정
		galleryDTO.setGalleryFileName("no_file");
		// 업로드된 파일이 없는 경우 확장자에 파일 없음 지정
		galleryDTO.setGalleryFileExtension("no_file");
		// 사진첩의 idx 지정
		galleryDTO.setGalleryIdx(idx);
		
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
		return "redirect:gallery.do?idx=" + idx;
	}
	
	// 사진첩 글 삭제
	@RequestMapping("/gallery_delete.do")
	@ResponseBody // Ajax로 요청된 메서드는 결과를 콜백 메서드로 돌려주기 위해 반드시 @ResponseBody가 필요!!
	public String delete(int galleryIdx, Gallery gallery) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 삭제 실패할 경우
		String result = "no";

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 게시글은 오로지 미니홈피 주인만 삭제할 수 있다.
		if ( (Integer) session.getAttribute("login") != galleryIdx ) {
			// 콜백 메소드에 전달
			return result;
		}
		
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
	@RequestMapping("/gallery_modify_form.do")
	public String modify_form(int galleryIdx, Gallery gallery, Model model) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 게시글은 오로지 미니홈피 주인만 수정할 수 있다.
		if ( (Integer) session.getAttribute("login") != galleryIdx ) {
			// 해당 미니홈피 유저의 사진첩 페이지로 이동
			return "redirect:gallery.do?idx=" + galleryIdx;
		}
		
		// 해당 idx의 사진첩에 수정할 게시글을 조회
		Gallery updateGallery = galleryService.findByGalleryIdxAndIdx(gallery);
		if ( updateGallery != null ) {
			// 게시글 DTO에 수정할 게시글 정보 전달
			GalleryDTO galleryDTO = new GalleryDTO(updateGallery);
			// 게시글 DTO 바인딩
			model.addAttribute("galleryDTO", galleryDTO);
		}
		
		// 수정 페이지로 이동
		return "Page/Gallery/gallery_modify_form";
	}
	
	// 게시글 수정하기
	@RequestMapping("/gallery_modify.do")
	public String modify(int galleryIdx, GalleryDTO galleryDTO) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}

		// 로그인한 유저의 idx와 해당 미니홈피 유저의 idx가 다를경우 - 게시글은 오로지 미니홈피 주인만 수정할 수 있다.
		if ( (Integer) session.getAttribute("login") != galleryIdx ) {
			// 해당 미니홈피 유저의 사진첩 페이지로 이동
			return "redirect:gallery.do?idx=" + galleryIdx;
		}

		// 클라이언트의 파일 업로드를 위해 절대 경로를 생성
		String savePath = "/Users/p._.sc/IT/Project/CyworldProject/util/files/gallery"; // 절대 경로
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
		return "redirect:gallery.do?idx=" + galleryDTO.getGalleryIdx();
	}
	
	/////////////// 사진첩 댓글 구역 ///////////////
	
	// 댓글 작성
	@RequestMapping("/comment_insert.do")
	@ResponseBody
	public String gallery_reply(GalleryComment galleryComment) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 로그인 페이지로 이동
			return "redirect:login.do";
		}
		
		// 세션값을 사용하기 위해 Integer타입으로 형변환
		Integer sessionIdx = (Integer) session.getAttribute("login");
		// 작성 시간을 기록하기 위해 Date객체 사용
		Date date = new Date();
		// Date객체를 원하는 모양대로 재조합
		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 댓글 Idx에 AUTO_INCREMENT로 null 지정
		galleryComment.setIdx(null);
		// 댓글의 삭제 여부 0 지정
		galleryComment.setGalleryCommentDeleteCheck(0);
		// 댓글의 작성자 idx 지정
		galleryComment.setGalleryCommentSessionIdx(sessionIdx);
		// 댓글에 작성 시간 지정
		galleryComment.setGalleryCommentRegDate(today.format(date));

		// 작성한 댓글을 저장
		GalleryComment res = galleryService.insertIntoGalleryComment(galleryComment);

		// 저장 실패할 경우
		String result = "no";
		if ( res != null ) {
			// 저장 성공할 경우
			result = "yes";
		}

		// 콜백 메소드에 전달
		return result;
	}
	
	// 댓글 삭제 - 완전 삭제가 아닌 삭제된 것처럼 만들기
	@RequestMapping("/gcomment_delete.do")
	@ResponseBody
	public String gcomment_delete(GalleryComment galleryComment) {
		// 댓글의 삭제 여부 -1 지정
		galleryComment.setGalleryCommentDeleteCheck(-1);

		// 삭제할 댓글의 삭제 여부만 갱신
		int res = galleryService.updateSetGalleryCommentDeleteCheckByGalleryCommentIdxAndGalleryIdxCommentAndIdx(galleryComment);

		// 삭제 실패할 경우
		String result = "no";
		if (res == 1) {
			// 삭제 성공할 경우
			result = "yes";
		}

		// 콜백 메소드에 전달
		return result;
	}
	
	/////////////// 사진첩 좋아요 구역 ///////////////
	
	@RequestMapping("/gallery_like.do")
	@ResponseBody // 콜백메소드에 VO를 전달
	public Gallery gallery_like(Gallery gallery, GalleryLike galleryLike) {
		// 세션값이 있는지 확인
		HttpSession session = request.getSession();
		if ( session.getAttribute("login") == null ) {
			// 세션값이 없다면 콜백메소드에 null을 전달
			return null;
		}
		System.out.println(session.getAttribute("login"));
		// 세션값을 사용하기 위해 Integer타입으로 형변환
		Integer sessionIdx = (Integer) session.getAttribute("login");


		// 좋아요를 누른 로그인한 유저의 세션값 지정
		galleryLike.setGalleryLikeSessionIdx(sessionIdx);
		// 사진첩의 idx 지정
		galleryLike.setGalleryLikeIdx(gallery.getGalleryIdx());
		// 좋아요를 누른 게시글의 번호
		galleryLike.setGalleryLikeRef(gallery.getIdx());

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
			// 좋아요 Idx에 AUTO_INCREMENT로 null 지정
			galleryLike.setIdx(null);
			// 좋아요를 누를 경우 좋아요 내역을 추가
			galleryService.insertIntoGalleryLike(galleryLike);
			// 좋아요 개수 조회
			int likeCount = galleryService.countByGalleryLikeIdxAndGalleryLikeRef(galleryLike);
			// 조회된 좋아요 개수를 지정
			gallery.setGalleryLikeNum(likeCount);
			// 조회된 좋아요 개수로 갱신
			galleryService.updateSetGalleryLikeNumByGalleryIdxIdxAndIdx(gallery);
			// 콜백 메소드에 VO를 전달
			return gallery;
		}
	}
}