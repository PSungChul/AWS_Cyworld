package com.social.cyworld.controller;

import com.social.cyworld.dto.SearchDTO;
import com.social.cyworld.dto.UserDTO;
import com.social.cyworld.entity.ChatRooms;
import com.social.cyworld.entity.Users;
import com.social.cyworld.service.UserDTOService;
import com.social.cyworld.util.JwtUtil;
import com.social.cyworld.util.MongoUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping("/chat")
@Controller
public class ChatController {
	// @Autowired
	@Autowired
	HttpServletRequest request;
	@Autowired
	HttpServletResponse response;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	MongoUtil mongoUtil;
	@Autowired
	UserDTOService userDTOService;

	// 채팅방 첫 입장 체크용 Map
	Map<Integer, List<Object>> enterCheckMap = new ConcurrentHashMap<>();
	// STOMP Socket에서 메시지를 보낼 때 퇴장 메시지와 재입장(새로고침) 메시지를 구분하기 위한 재입장(새로고침) 체크용 Map
	Map<Integer, String> reEnterCheck = new ConcurrentHashMap<>();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 채팅 페이지
	@RequestMapping("/{idx}")
	public String chat(@PathVariable int idx, Model model) {
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
				// 채팅 페이지로 이동
				return "Page/chat";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 채팅 페이지로 이동
				return "Page/chat";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 채팅 페이지로 이동
			return "Page/chat";
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
				// 채팅 페이지로 이동
				return "Page/chat";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 채팅 페이지로 이동
					return "Page/chat";
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

		// 토큰에서 추출한 로그인 유저 idx와 채팅에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != idx ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 채팅 페이지로 이동
			return "Page/chat";
		}

		// 로그인 유저 idx에 해당하는 채팅방을 모두 조회하여 리스트로 가져온다.
		List<Users.ChatRoomList> chatRoomList = mongoUtil.findAllChatRoomList(loginIdx);
		// 가져온 채팅방 리스트를 바인딩한다.
		model.addAttribute("chatRoomList", chatRoomList);

		// 로그인 유저 idx를 바인딩한다.
		model.addAttribute("loginIdx", loginIdx);

		// 채팅 페이지로 이동
		return "Page/chat";
	}

	// 이름 및 ID 및 Email로 유저 검색
	@RequestMapping("/chat_search")
	@ResponseBody
	public List<SearchDTO> chatSearch(SearchDTO searchDTO, Model model) {
		// 반환용 리스트
		List<SearchDTO> list = new ArrayList<>();
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
				// 검색 DTO idx에 에러 코드를 setter를 통해 전달한다.
				searchDTO.setIdx(0);
				// 전달받은 전달 DTO를 반환용 리스트에 전달한다.
				list.add(searchDTO);
				// 전달받은 반환용 리스트를 반환한다.
				return list;
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 검색 DTO idx에 에러 코드를 setter를 통해 전달한다.
				searchDTO.setIdx(-4);
				// 전달받은 전달 DTO를 반환용 리스트에 전달한다.
				list.add(searchDTO);
				// 전달받은 반환용 리스트를 반환한다.
				return list;
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 검색 DTO idx에 에러 코드를 setter를 통해 전달한다.
			searchDTO.setIdx(-99);
			// 전달받은 전달 DTO를 반환용 리스트에 전달한다.
			list.add(searchDTO);
			// 전달받은 반환용 리스트를 반환한다.
			return list;
		}
		// idx가 에러 코드 -1인 경우 - 토큰 만료
		if ( loginIdx == -1 ) {
			// 세션이 존재하는지 체크한다.
			HttpSession session = request.getSession();
			// 세션이 존재하지 않는 경우 - 대기 시간 1시간 이후
			if ( session.getAttribute("login") == null ) {
				// 토큰과 리프레쉬 토큰을 삭제한다.
				jwtUtil.logoutToken(authorization);
				// 검색 DTO idx에 에러 코드를 setter를 통해 전달한다.
				searchDTO.setIdx(-1);
				// 전달받은 전달 DTO를 반환용 리스트에 전달한다.
				list.add(searchDTO);
				// 전달받은 반환용 리스트를 반환한다.
				return list;
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 검색 DTO idx에 에러 코드를 setter를 통해 전달한다.
					searchDTO.setIdx(-100);
					// 전달받은 전달 DTO를 반환용 리스트에 전달한다.
					list.add(searchDTO);
					// 전달받은 반환용 리스트를 반환한다.
					return list;
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

		// 토큰에서 추출한 로그인 유저 idx와 채팅에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != searchDTO.getLoginUserIdx() ) {
			// 검색 DTO idx에 에러 코드를 setter를 통해 전달한다.
			searchDTO.setIdx(-4);
			// 전달받은 전달 DTO를 반환용 리스트에 전달한다.
			list.add(searchDTO);
			// 전달받은 반환용 리스트를 반환한다.
			return list;
		}

		// 검색한 이름에 해당하는 모든 유저를 조회하여 리스트로 가져온다.
		list = userDTOService.findChatSignJoinUserProfileJoinUserMainByNameLike(searchDTO.getSearchValue());

		// 채팅 페이지로 이동
		return list;
	}

	// 입장
	@RequestMapping("/{loginUserIdx}/room/{idx}")
	public String chatRoom(@PathVariable int loginUserIdx,
						   @PathVariable int idx,
						   @RequestParam(required = false) String id,
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
				// 채팅 페이지로 이동
				return "Page/chat";
			// 토큰도 세션도 존재하지 않는 경우 - 에러
			} else {
				// 에러 메시지를 바인딩한다.
				model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
				// 채팅 페이지로 이동
				return "Page/chat";
			}
		}
		// 쿠키에 토큰이 존재하는 경우 - 로그인 유저
		// JWT에서 토큰에 해당하는 로그인 유저 idx를 추출한다.
		int loginIdx = jwtUtil.validationToken(authorization);
		// idx가 에러 코드 -99인 경우
		if ( loginIdx == -99 ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "다른 곳에서 로그인이 시도되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
			// 채팅 페이지로 이동
			return "Page/chat";
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
				// 채팅 페이지로 이동
				return "Page/chat";
			// 세션이 존재하는 경우 - 대기 시간 1시간 이전
			} else {
				// JWT에서 리프레쉬 토큰으로 토큰을 재생성한다.
				String refreshToken = jwtUtil.validationRefreshToken(authorization);
				// 리프레쉬 토큰으로 토큰이 재생성 됬는지 체크한다.
				// 토큰이 재생성 안된 경우 - 리프레쉬 토큰 만료
				if ( refreshToken == null ) {
					// 에러 메시지를 바인딩한다.
					model.addAttribute("errMsg", "로그인 시간이 만료되어 로그인 페이지로 이동합니다.\n다시 로그인 해주시기 바랍니다.");
					// 채팅 페이지로 이동
					return "Page/chat";
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

		// 토큰에서 추출한 로그인 유저 idx와 채팅에서 가져온 로그인 유저 idx가 다른 경우 - 유효성 검사
		if ( loginIdx != loginUserIdx ) {
			// 에러 메시지를 바인딩한다.
			model.addAttribute("errMsg", "잘못된 접근입니다.\n다시 로그인 해주시기 바랍니다.");
			// 채팅 페이지로 이동
			return "Page/chat";
		}

		// 검색으로 들어온 경우 - 채팅방 아이디 존재 X
		if ( id == "" ) {
			// 로그인 유저 idx와 상대 유저 idx에 해당하는 채팅방 정보를 조회한다.
			Users user = mongoUtil.findChatRoom(loginIdx, idx);

			// 조회한 채팅방 정보가 없는 경우
			if ( user == null ) {
				// 채팅방 첫 입장 체크용 Map에 상대 유저 idx에 해당하는 첫 입장 채팅방 정보가 존재하는지 체크한다.
				// 첫 입장 채팅방 정보가 존재하는 경우
				if ( enterCheckMap.get(idx) != null ) {
					// 첫 입장 채팅방 정보 중 유저 idx가 로그인 유저 idx와 같은지 체크한다.
					// 로그인 유저 idx와 같은 경우
					if ( (int) enterCheckMap.get(idx).get(0) == loginIdx ) {
						// 첫 입장 채팅방 정보 중 채팅방 아이디를 가져와 id에 전달한다.
						id = (String) enterCheckMap.get(idx).get(1);

						// 첫 입장 채팅방 정보 List를 생성한다.
						List<Object> enterCheckList = new ArrayList<>();
						// 첫 입장 채팅방 정보 List에 상대 유저 idx를 추가한다.
						enterCheckList.add(idx);
						// 첫 입장 채팅방 정보 List에 전달받은 채팅방 아이디를 추가한다.
						enterCheckList.add(id);
						// 로그인 유저 idx를 키로 사용하고, 첫 입장 채팅방 정보 List를 값으로 사용하여, 채팅방 첫 입장 체크용 Map에 추가한다.
						enterCheckMap.put(loginIdx, enterCheckList);

						// 전달받은 채팅방 아이디를 가지고 다시 입장한다.
						return "redirect:/chat/" + loginIdx + "/room/" + idx + "?id=" + id;
					}
				}

				// 첫 입장 채팅방 정보가 존재하지 않는 경우

				// 로그인 유저 idx + 현재 시간 + 상대 유저 idx로 채팅방 아이디를 생성한다.
				id = "" + loginUserIdx + System.currentTimeMillis() + idx;

				// 첫 입장 채팅방 정보 List를 생성한다.
				List<Object> enterCheckList = new ArrayList<>();
				// 첫 입장 채팅방 정보 List에 상대 유저 idx를 추가한다.
				enterCheckList.add(idx);
				// 첫 입장 채팅방 정보 List에 생성한 채팅방 아이디를 추가한다.
				enterCheckList.add(id);
				// 로그인 유저 idx를 키로 사용하고, 첫 입장 채팅방 정보 List를 값으로 사용하여, 채팅방 첫 입장 체크용 Map에 추가한다.
				enterCheckMap.put(loginIdx, enterCheckList);

				// 생성한 채팅방 아이디를 가지고 다시 입장한다.
				return "redirect:/chat/" + loginIdx + "/room/" + idx + "?id=" + id;
			// 조회한 채팅방 정보가 있는 경우
			} else {
				// 조회한 채팅방 정보 중 채팅방 아이디를 가져와 id에 전달한다.
				id = user.getChatRooms().get(0).get_id();

				// 전달받은 채팅방 아이디에 해당하는 채팅방 정보에서 상대 메시지 상태를 읽음 상태로 갱신한다.
				mongoUtil.updateChatStatus(id, loginIdx);

				// 전달받은 채팅방 아이디에 해당하는 메시지를 모두 조회하여 리스트로 가져온다.
				List<ChatRooms.ChatMessageList> chatMessageList = mongoUtil.findAllChatMessageList(id);
				// 가져온 메시지 리스트를 바인딩한다.
				model.addAttribute("chatMessageList", chatMessageList);
			}
		// 채팅방 리스트로 들어온 경우 - 채팅방 아이디 존재 O
		} else {
			// 채팅방 아이디에 해당하는 채팅방 정보에서 상대 메시지 상태를 읽음 상태로 갱신한다.
			mongoUtil.updateChatStatus(id, loginIdx);

			// 채팅방 아이디에 해당하는 메시지를 모두 조회하여 리스트로 가져온다.
			List<ChatRooms.ChatMessageList> chatMessageList = mongoUtil.findAllChatMessageList(id);
			// 가져온 메시지 리스트를 바인딩한다.
			model.addAttribute("chatMessageList", chatMessageList);
		}

		// 로그인 유저 idx에 해당하는 채팅방 페이지 유저 정보를 조회한다.
		UserDTO userDTO = userDTOService.findChatRoomByIdx(loginIdx);
		// 조회한 채팅방 페이지 유저 정보 DTO를 바인딩한다.
		model.addAttribute("sign", userDTO);

		// 상대 유저 idx를 바인딩한다.
		model.addAttribute("userIdx", idx);

		// MongoDB에 상대 유저 idx에 해당하는 유저 정보가 존재하는지 체크하여 존재하지 않다는면 상대 유저 정보를 저장한다.
		mongoUtil.findAllChatRoomList(idx);

		// 로그인 유저 idx에 해당하는 채팅방을 모두 조회하여 리스트로 가져온다.
		List<Users.ChatRoomList> chatRoomList = mongoUtil.findAllChatRoomList(loginIdx);
		// 가져온 채팅방 리스트를 바인딩한다.
		model.addAttribute("chatRoomList", chatRoomList);

		// 채팅방 아이디를 바인딩한다.
		model.addAttribute("id", id);

		// 세션을 가져와 입장 체크용 세션이 존재하는지 체크한다.
		HttpSession session = request.getSession();
		// 입장 체크용 세션이 존재하지 않는 경우
		if ( session.getAttribute("id") == null ) {
			// 채팅방 아이디를 사용하여 입장 체크용 세션을 생성한다.
			session.setAttribute("id", id);
		// 입장 체크용 세션이 존재하는 경우
		} else {
			// 입장 체크용 세션에 들어있는 채팅방 아이디가 입장하는 채팅방 아이디와 같은지 체크한다.
			// 채팅방 아이디가 같은 경우
			if ( session.getAttribute("id").equals(id) ) {
				// 로그인 유저 idx를 키로 사용하고, 채팅방 아이디를 값으로 사용하여, 입장 체크용 Map에 추가한다.
				reEnterCheck.put(loginIdx, id);
				// 입장 체크용 값으로 채팅방 아이디를 바인딩한다.
				model.addAttribute("entryCheck", id);
			// 채팅방 아이디가 다른 경우
			} else {
				// 입장 체크용 세션을 삭제한다.
				session.removeAttribute("id");
				// 채팅방 아이디를 가지고 다시 입장한다.
				return "redirect:/chat/" + loginIdx + "/room/" + idx + "?id=" + id;
			}
		}

		// 채팅방 페이지로 이동
		return "Page/chat_room";
	}
}