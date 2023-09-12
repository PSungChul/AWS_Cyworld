package com.social.cyworld.controller;

import com.social.cyworld.dto.ChatMessageDTO;
import com.social.cyworld.dto.UserDTO;
import com.social.cyworld.entity.ChatRooms;
import com.social.cyworld.entity.Users;
import com.social.cyworld.service.UserDTOService;
import com.social.cyworld.util.MongoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequiredArgsConstructor
public class StompController {
	// @MessageMapping으로 받아온 메시지를 다시 클라이언트로 전달해주는 SimpMessagingTemplate
	@Autowired
	SimpMessagingTemplate template; // 특정 Broker로 메시지를 전달한다.
	@Autowired
	MongoUtil mongoUtil;
	@Autowired
	ChatController chatController;
	@Autowired
	UserDTOService userDTOService;

	// 참여자 수 체크용 Map
	Map<String, AtomicInteger> participantsMap = new ConcurrentHashMap<>();
	// 채팅방 유저 정보 Map
	Map<Integer, List<HashMap<String, Object>>> chatRoomMap = new ConcurrentHashMap<>();
	// 첫 메시지 체크용 Map
	Map<Integer, Integer> messageCheck = new HashMap<>();

	// Client에서 전송한 SEND 요청을 처리
	// @MessageMapping - 클라이언트에서 요청을 보낸 URI 에 대응하는 메소드로 연결을 해주는 역할을 한다.
	// StompWebSocketConfig에서 설정한 applicationDestinationPrefixes와 @MessageMapping 경로가 자동으로 병합된다.
	// "/pub" + "/chat/enter" = "/pub/chat/enter"
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 채팅방 첫 입장
	@MessageMapping(value = "/chat/enter")
	public void enterStudyRoom(ChatMessageDTO message) { // 클라이언트로부터 전송된 첫 입장 정보들을 DTO로 받아온다.
		// 채팅방 유저 정보 List를 생성한다.
		List<HashMap<String, Object>> userList = new ArrayList<>();

		// 로그인 유저 정보 Map을 생성한다.
		HashMap<String, Object> loginUserMap = new HashMap<>();
		// 로그인 유저 idx에 해당하는 채팅방 페이지 유저 정보를 조회한다.
		UserDTO loginUserDTO = userDTOService.findChatRoomByIdx(message.getIdx());
		// id를 키로 사용하고, 채팅방 아이디를 값으로 사용하여, 로그인 유저 정보 Map에 추가한다.
		loginUserMap.put("id", message.getId());
		// idx를 키로 사용하고, 조회한 유저 정보 중 유저 idx를 가져와 값으로 사용하여, 로그인 유저 정보 Map에 추가한다.
		loginUserMap.put("idx", loginUserDTO.getIdx());
		// email을 키로 사용하고, 조회한 유저 정보 중 이메일을 가져와 값으로 사용하여, 로그인 유저 정보 Map에 추가한다.
		loginUserMap.put("email", loginUserDTO.getEmail());
		// name을 키로 사용하고, 조회한 유저 정보 중 이름을 가져와 값으로 사용하여, 로그인 유저 정보 Map에 추가한다.
		loginUserMap.put("name", loginUserDTO.getName());
		// mainPhoto를 키로 사용하고, 조회한 유저 정보 중 메인 사진을 가져와 값으로 사용하여, 로그인 유저 정보 Map에 추가한다.
		loginUserMap.put("mainPhoto", loginUserDTO.getMainPhoto());
		// 채팅방 아이디와 로그인 유저 정보를 추가한 로그인 유저 정보 Map을 채팅방 유저 정보 List에 추가한다.
		userList.add(loginUserMap);

		// 상대 유저 정보 Map을 생성한다.
		HashMap<String, Object> userMap = new HashMap<>();
		// 상대 유저 idx에 해당하는 채팅방 페이지 유저 정보를 조회한다.
		UserDTO userDTO = userDTOService.findChatRoomByIdx(message.getUserIdx());
		// id를 키로 사용하고, 채팅방 아이디를 값으로 사용하여, 상대 유저 정보 Map에 추가한다.
		userMap.put("id", message.getId());
		// idx를 키로 사용하고, 조회한 유저 정보 중 유저 idx를 가져와 값으로 사용하여, 상대 유저 정보 Map에 추가한다.
		userMap.put("idx", userDTO.getIdx());
		// email을 키로 사용하고, 조회한 유저 정보 중 이메일을 가져와 값으로 사용하여, 상대 유저 정보 Map에 추가한다.
		userMap.put("email", userDTO.getEmail());
		// name을 키로 사용하고, 조회한 유저 정보 중 이름을 가져와 값으로 사용하여, 상대 유저 정보 Map에 추가한다.
		userMap.put("name", userDTO.getName());
		// mainPhoto를 키로 사용하고, 조회한 유저 정보 중 메인 사진을 가져와 값으로 사용하여, 상대 유저 정보 Map에 추가한다.
		userMap.put("mainPhoto", userDTO.getMainPhoto());
		// 채팅방 아이디와 상대 유저 정보를 추가한 상대 유저 정보 Map을 채팅방 유저 정보 List에 추가한다.
		userList.add(userMap);

		// 로그인 유저 idx를 키로 사용하고, 로그인 유저 정보와 상대 유저 정보를 추가한 채팅방 유저 정보 List를 값으로 사용하여, 채팅방 유저 정보 Map에 추가한다.
		chatRoomMap.put(message.getIdx(), userList);

		// 참여자 수 체크용 Map에 채팅방 아이디에 해당하는 참여자 수가 존재하는지 체크한다.
		// 참여자 수가 존재 하지 않는 경우
		if ( participantsMap.get(message.getId()) == null ) {
			// 채팅방 아이디를 키로 사용하고, 참여자 수를 값으로 사용하여, 참여자 수 체크용 Map에 추가한다.
			participantsMap.put(message.getId(), new AtomicInteger(1));
		// 참여자 수가 존재 하는 경우
		} else {
			// 채팅방 아이디에 해당하는 참여자 수를 1 증가시킨다.
			participantsMap.get(message.getId()).incrementAndGet();
		}

		// 메시지 DTO 중 참여자 수에 참여자 수 체크용 Map에서 채팅방 아이디에 해당하는 참여자 수를 가져와 setter를 통해 전달한다.
		message.setParticipants(participantsMap.get(message.getId()).get());

		// SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 파라미터로 받아온 메시지 DTO를 다시 전달한다.
		// path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 파라미터로 받아온 메시지 DTO 중 채팅방 아이디가 병합된다.
		// "/sub" + "/chat/" + id = "/sub/chat/1"
		template.convertAndSend("/sub/chat/" + message.getId(), message);
	}

	// 채팅방 채팅
	@MessageMapping(value = "/chat/message")
	public void messageStudyRoom(ChatMessageDTO message) { // 클라이언트로부터 전송된 채팅 정보들을 DTO로 받아온다.
		// 첫 메시지 체크용 Map에 첫 메시지 체크 값이 존재하는지 체크한다.
		// 첫 메시지 체크 값이 존재하지 않는 경우
		if ( messageCheck.get(message.getIdx()) == null ) {
			// 채팅방 아이디에 해당하는 채팅방 정보가 존재하는지 체크하여 존재하지 않는다면 저장한다.
			mongoUtil.insertFirstChatMessage(message.getId());
			// 채팅방 아이디를 키로 사용하고, 1을 값으로 사용하여, 첫 메시지 체크용 Map에 추가한다.
			messageCheck.put(message.getIdx(), 1);

			// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에 로그인 유저 idx에 해당하는 채팅방 정보가 존재하는지 체크한다.
			// 채팅방 정보가 존재하는 경우
			if ( chatController.enterCheckMap.get(message.getIdx()) != null ) {
				// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에서 로그인 유저 idx에 해당하는 채팅방 정보를 삭제한다.
				chatController.enterCheckMap.remove(message.getIdx());

				// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에 로그인 유저 idx에 해당하는 채팅방 유저 정보 List 중 상대 유저 정보 Map에 상대 유저 idx에 해당하는 채팅방 정보가 존재하는지 체크한다.
				// 채팅방 정보가 존재하는 경우
				if ( chatController.enterCheckMap.get((int) chatRoomMap.get(message.getIdx()).get(1).get("idx")) != null ) {
					// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에서 로그인 유저 idx에 해당하는 채팅방 유저 정보 List 중 상대 유저 정보 Map에서 상대 유저 idx에 해당하는 채팅방 정보를 삭제한다.
					chatController.enterCheckMap.remove((int) chatRoomMap.get(message.getIdx()).get(1).get("idx"));
				}
			}
		}

		// 첫 메시지 체크 값이 존재하는 경우

		// 메시지 DTO 중 참여자 수에 참여자 수 체크용 Map에서 채팅방 아이디에 해당하는 참여자 수를 가져와 setter를 통해 전달한다.
		message.setParticipants(participantsMap.get(message.getId()).get());

		// 전달받은 참여자 수에 따라 메시지 상태를 설정하여 채팅 메시지를 저장하고, 채팅방 정보를 최신으로 유지한다.
		// 전달받은 참여자 수가 2인 경우
		if ( message.getParticipants() == 2 ) {
			// 메시지 DTO 중 메시지 상태에 읽음을 의미하는 0을 setter를 통해 전달한다.
			message.setStatus("0");
			// 채팅방 아이디에 해당하는 채팅방 정보에 채팅 메시지를 저장한다.
			mongoUtil.insertChatMessage(message.getId(), ChatRooms.ChatMessageList.toMessage(chatRoomMap.get(message.getIdx()).get(0), message));
			// 로그인 유저 idx에 해당하는 유저 정보 중 상대 유저 정보에 해당하는 채팅방 정보를 삭제 후 새로 저장하여 현재 채팅 중인 채팅방을 최신 상태로 유지한다.
			mongoUtil.deleteAndInsertChatRoom(message.getIdx(), Users.ChatRoomList.toEntity(chatRoomMap.get(message.getIdx()).get(1)));
			// 메시지 DTO 중 메시지 상태에 읽음을 나타내기 위해 아무 값도 설정하지 않는다.
			message.setStatus("");
		// 전달받은 참여자 수가 1인 경우
		} else if ( message.getParticipants() == 1 ) {
			// 메시지 DTO 중 메시지 상태에 안 읽음을 의미하는 1을 setter를 통해 전달한다.
			message.setStatus("1");
			// 채팅방 아이디에 해당하는 채팅방 정보에 채팅 메시지를 저장한다.
			mongoUtil.insertChatMessage(message.getId(), ChatRooms.ChatMessageList.toMessage(chatRoomMap.get(message.getIdx()).get(0), message));
			// 로그인 유저 idx에 해당하는 유저 정보 중 상대 유저 정보에 해당하는 채팅방 정보를 삭제 후 새로 저장하여 현재 채팅 중인 채팅방을 최신 상태로 유지한다.
			mongoUtil.deleteAndInsertChatRoom(message.getIdx(), Users.ChatRoomList.toEntity(chatRoomMap.get(message.getIdx()).get(1)));
			// 채팅방 아이디에 해당하는 채팅방 정보 중 로그인 유저 idx에 해당하는 안 읽은 메시지 수를 구한다.
			int count = mongoUtil.countChatStatus(message.getId(), message.getIdx());
			// 상대 유저 idx에 해당하는 유저 정보 중 상대 유저 정보에 해당하는 채팅방 정보를 삭제 후 안 읽은 메시지 수와 함께 새로 저장하여 현재 채팅 중인 채팅방을 최신 상태로 유지한다.
			mongoUtil.deleteAndInsertChatRoom((int) chatRoomMap.get(message.getIdx()).get(1).get("idx"), Users.ChatRoomList.toEntity(chatRoomMap.get(message.getIdx()).get(0)), count);
		}

		// 채팅방 유저 정보 Map에서 로그인 유저 idx에 해당하는 로그인 유저 정보Map을 가져와 메시지 DTO로 변환한다.
		message = message.toChatMessage(chatRoomMap.get(message.getIdx()).get(0));

		// SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 메시지 파라미터로 받아온 메시지 DTO를 다시 전달한다.
		// path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 파라미터로 받아온 메시지 DTO 중 채팅방 아이디가 병합된다.
		// "/sub" + "/chat/" + id = "/sub/chat/1"
		template.convertAndSend("/sub/chat/" + message.getId(), message);
	}

	// 스터디룸 녹음
	@MessageMapping(value = "/chat/record")
	public void recordStudyRoom(@Payload String recordFile, SimpMessageHeaderAccessor accessor) { // 클라이언트로부터 전송된 녹음된 오디오 데이터를 @Payload로 받아온다.
		/***************************************************************************************************************
		 @Payload - Spring 프레임워크에서 메시지 페이로드를 메소드 파라미터와 매핑할 때 사용되는 어노테이션이다.
		 			@Payload가 붙은 파라미터는 메시지 페이로드 자체를 나타내며, Spring은 메시지 변환기(MessageConverter)를 사용하여 해당 객체로 변환한다.
		 			일반적으로 Spring WebSocket이나 STOMP 메시지를 처리하는 메서드에서 많이 사용되며,
		 			이 어노테이션을 사용하면 개발자가 메시지 변환에 필요한 코드를 작성하지 않아도 되므로 코드 간결성과 생산성을 높일 수 있다.
		 SimpMessageHeaderAccessor - Spring 프레임워크의 MessageHeaderAccessor 인터페이스를 구현한 클래스로, STOMP 메시지의 헤더 정보를 쉽게 조작하고 접근할 수 있게 해준다.
		 							 STOMP 프로토콜에서 사용되는 프레임(Frame)에 대한 정보를 포함하며, 이를 이용하여 메시지의 발신자, 수신자, 메시지 타입 등의 정보를 설정하거나 읽어올 수 있다.
		 							 STOMP 메시지에 사용될 헤더의 추가, 수정, 삭제, 조회 등을 할 수 있다.
		 ***************************************************************************************************************/

		// SimpMessageHeaderAccessor를 사용하여 "id"라는 이름의 헤더 정보를 추출해, id 변수에 채팅방 아이디를 전달한다.
		String id = accessor.getFirstNativeHeader("id");
		// SimpMessageHeaderAccessor를 사용하여 "idx"라는 이름의 헤더 정보를 추출해, idx 변수에 로그인 유저 idx를 전달한다.
		int idx = Integer.valueOf(accessor.getFirstNativeHeader("idx"));

		// 첫 메시지 체크용 Map에 첫 메시지 체크 값이 존재하는지 체크한다.
		// 첫 메시지 체크 값이 존재하지 않는 경우
		if ( messageCheck.get(idx) == null ) {
			// 채팅방 아이디에 해당하는 채팅방 정보가 존재하는지 체크하여 존재하지 않는다면 저장한다.
			mongoUtil.insertFirstChatMessage(id);
			// 채팅방 아이디를 키로 사용하고, 1을 값으로 사용하여, 첫 메시지 체크용 Map에 추가한다.
			messageCheck.put(idx, 1);

			// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에 로그인 유저 idx에 해당하는 채팅방 정보가 존재하는지 체크한다.
			// 채팅방 정보가 존재하는 경우
			if ( chatController.enterCheckMap.get(idx) != null ) {
				// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에서 로그인 유저 idx에 해당하는 채팅방 정보를 삭제한다.
				chatController.enterCheckMap.remove(idx);

				// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에 로그인 유저 idx에 해당하는 채팅방 유저 정보 List 중 상대 유저 정보 Map에 상대 유저 idx에 해당하는 채팅방 정보가 존재하는지 체크한다.
				// 채팅방 정보가 존재하는 경우
				if ( chatController.enterCheckMap.get((int) chatRoomMap.get(idx).get(1).get("idx")) != null ) {
					// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에서 로그인 유저 idx에 해당하는 채팅방 유저 정보 List 중 상대 유저 정보 Map에서 상대 유저 idx에 해당하는 채팅방 정보를 삭제한다.
					chatController.enterCheckMap.remove((int) chatRoomMap.get(idx).get(1).get("idx"));
				}
			}
		}

		// 첫 메시지 체크 값이 존재하는 경우

		/***************************************************************************************************************
		 SimpMessageHeaderAccessor 클래스의 create() 메소드를 사용하여 headers 객체를 생성한다.
		 SimpMessageHeaderAccessor - Spring 프레임워크의 메시지 핸들러에서 메시지의 헤더 정보를 읽거나 쓸 수 있도록 지원하는 클래스이다.
		 create() - 새로운 SimpMessageHeaderAccessor 객체를 생성하고 반환한다.
		 이렇게 생성된 SimpMessageHeaderAccessor 객체를 통해 메시지 헤더에 접근하여 정보를 설정하거나 조회할 수 있다.
		 ***************************************************************************************************************/
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();

		/***************************************************************************************************************
		 setLeaveMutable(true) - SimpMessageHeaderAccessor 객체가 생성될 때 생성되는 내부 MessageHeaders 객체를 변경 가능하게 설정하는 메소드이다.
		 MessageHeaders 객체에는 메시지 헤더와 관련된 정보가 들어있으며, 이 정보들을 변경할 수 있도록 하기 위해 setLeaveMutable() 메소드를 호출하여 변경 가능하게 설정한다.
		 이후 SimpMessageHeaderAccessor 객체를 사용하여 메시지 헤더에 정보를 추가하거나 제거할 수 있다.
		 ***************************************************************************************************************/
		headers.setLeaveMutable(true);

		/***************************************************************************************************************
		 setNativeHeader("key", "value") - SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header를 설정하는 메소드로, 이 메소드를 사용하기 위해서는 key와 value를 인자로 전달해주어야 한다.
		 이렇게 key와 value로 설정된 native header는 STOMP 프로토콜을 사용하는 메시지 전송 시에 추가적인 헤더 정보를 담을 때 사용된다.
		 ***************************************************************************************************************/
		// SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "type"에 "record"를 설정하여, 클라이언트 측에서 해당 메시지를 녹음 메시지로 인식하고 처리할 수 있도록 한다.
		headers.setNativeHeader("type", "record");
		// SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "idx"에 헤더 정보를 추출해 전달받은 로그인 유저 idx를 설정하여, 클라이언트 측에서 해당 메시지를 보낸 유저 idx를 인식하고 처리할 수 있도록 한다.
		headers.setNativeHeader("idx", String.valueOf(idx));
		// SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "sender"에 로그인 유저 idx에 해당하는 채팅방 유저 정보 List 중 로그인 유저 정보 Map에 로그인 유저 이름을 가져와 설정하여, 클라이언트 측에서 해당 메시지를 누가 보냈는지 인식하고 처리할 수 있도록 한다.
		headers.setNativeHeader("sender", (String) chatRoomMap.get(idx).get(0).get("name"));
		// SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "mainPhoto"에 로그인 유저 idx에 해당하는 채팅방 유저 정보 List 중 로그인 유저 정보 Map에 로그인 유저 메인 사진을 가져와 설정하여, 클라이언트 측에서 해당 메시지 전송자의 메인 사진을 인식하고 처리할 수 있도록 한다.
		headers.setNativeHeader("mainPhoto", (String) chatRoomMap.get(idx).get(0).get("mainPhoto"));
		// SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "participants"에 참여자 수 체크용 Map에서 채팅방 아이디에 해당하는 참여자 수를 가져와 설정하여, 클라이언트 측에서 해당 채팅방 참여자 수를 인식하고 처리할 수 있도록 한다.
		headers.setNativeHeader("participants", String.valueOf(participantsMap.get(id).get()));

		// 참여자 수 체크용 Map에서 채팅방 아이디에 해당하는 참여자 수에 따라 메시지 상태를 설정하여 녹음 메시지를 저장하고, 채팅방 정보를 최신으로 유지한다.
		// 참여자 수가 2인 경우
		if ( participantsMap.get(id).get() == 2 ) {
			// 채팅방 아이디에 해당하는 채팅방 정보에 녹음 메시지를 저장한다.
			mongoUtil.insertChatMessage(id, ChatRooms.ChatMessageList.toRecord(chatRoomMap.get(idx).get(0), recordFile, "0"));
			// 로그인 유저 idx에 해당하는 유저 정보 중 상대 유저 정보에 해당하는 채팅방 정보를 삭제 후 새로 저장하여 현재 채팅 중인 채팅방을 최신 상태로 유지한다.
			mongoUtil.deleteAndInsertChatRoom(idx, Users.ChatRoomList.toEntity(chatRoomMap.get(idx).get(1)));
			// SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "status"에 읽음을 나타내기 위해 아무 값도 설정하지 않아서, 클라이언트 측에서 해당 메시지 상태를 인식하고 처리할 수 있도록 한다.
			headers.setNativeHeader("status", "");
		// 참여자 수가 1인 경우
		} else if ( participantsMap.get(id).get() == 1 ) {
			// 채팅방 아이디에 해당하는 채팅방 정보에 녹음 메시지를 저장한다.
			mongoUtil.insertChatMessage(id, ChatRooms.ChatMessageList.toRecord(chatRoomMap.get(idx).get(0), recordFile, "1"));
			// 로그인 유저 idx에 해당하는 유저 정보 중 상대 유저 정보에 해당하는 채팅방 정보를 삭제 후 새로 저장하여 현재 채팅 중인 채팅방을 최신 상태로 유지한다.
			mongoUtil.deleteAndInsertChatRoom(idx, Users.ChatRoomList.toEntity(chatRoomMap.get(idx).get(1)));
			// 채팅방 아이디에 해당하는 채팅방 정보 중 로그인 유저 idx에 해당하는 안 읽은 메시지 수를 구한다.
			int count = mongoUtil.countChatStatus(id, idx);
			// 상대 유저 idx에 해당하는 유저 정보 중 상대 유저 정보에 해당하는 채팅방 정보를 삭제 후 안 읽은 메시지 수와 함께 새로 저장하여 현재 채팅 중인 채팅방을 최신 상태로 유지한다.
			mongoUtil.deleteAndInsertChatRoom((int) chatRoomMap.get(idx).get(1).get("idx"), Users.ChatRoomList.toEntity(chatRoomMap.get(idx).get(0)), count);
			// SimpMessageHeaderAccessor를 사용하여 STOMP 메시지의 native header 중 "status"에 안 읽음을 의미하는 1을 설정하여, 클라이언트 측에서 해당 메시지 상태를 인식하고 처리할 수 있도록 한다.
			headers.setNativeHeader("status", "1");
		}

		// SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 파라미터로 받아온 메시지 DTO를 다시 전달한다.
		// path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 헤더 정보를 추출해 전달받은 채팅방 아이디가 병합된다.
		// "/sub" + "/chat/" + id = "/sub/chat/1"
		template.convertAndSend("/sub/chat/" + id, recordFile, headers.getMessageHeaders());
	}

	// 채팅방 퇴장
	@MessageMapping(value = "/chat/exit")
	public void exitStudyRoom(ChatMessageDTO message) { // 클라이언트로부터 전송된 퇴장 정보들을 DTO로 받아온다.
		/***************************************************************************************************************
		 새로고침이 서버를 거치는 순서가 ChatController의 채팅방 입장 메소드를 통해 재입장을 한 뒤에 여기 StompController의 채팅방 퇴장 메소드에 도달한다.
		 이를 이용하여 새로고침으로 재입장할때 입장 메소드에서 새로고침 체크 값을 넣어두면 여기서 확인이 가능하기에,
		 퇴장 메소드에서는 항상 새로고침 체크 값을 확인하여, 새로고침과 퇴장을 구분 지을 수 있다.
		 새로고침 체크 값이 존재하면 이는 새로고침으로 재입장한 것이며, 반대로 새로고침 체크 값이 존재하지 않다면 이는 퇴장한 것이다.
		 ***************************************************************************************************************/

		// ChatController에 존재하는 새로고침 체크용 Map에 로그인 유저 idx에 해당하는 새로고침 체크 값이 존재하는지 체크한다.
		// 새로고침 체크 값이 존재하는 경우 - 재입장
		if ( chatController.reEnterCheck.get(message.getIdx()) != null ) {
			// ChatController에 존재하는 새로고침 체크용 Map에서 로그인 유저 idx에 해당하는 새로고침 체크 값을 제거해 다시 새로고침 할 경우 새로고침 체크 값을 받을 수 있게 만든다.
			chatController.reEnterCheck.remove(message.getIdx());
		// 새로고침 체크 값이 존재하지 않는 경우 - 퇴장
		} else {
			// 참여자 수를 감소시킨 후 남은 참여자 수가 0 이하인 경우
			if ( participantsMap.get(message.getId()).decrementAndGet() <= 0 ) {
				// 참여자 수 체크용 Map에서 채팅방 아이디에 해당하는 참여자 수를 삭제한다.
				participantsMap.remove(message.getId());
			} else {
				// 메시지 DTO 중 참여자 수에 참여자 수 체크용 Map에서 채팅방 아이디에 해당하는 참여자 수를 가져와 setter를 통해 전달한다.
				message.setParticipants(participantsMap.get(message.getId()).get());
			}

			// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에 로그인 유저 idx에 해당하는 채팅방 정보가 존재하는지 체크한다.
			// 채팅방 정보가 존재하는 경우
			if ( chatController.enterCheckMap.get(message.getIdx()) != null ) {
				// ChatController에 존재하는 채팅방 첫 입장 체크용 Map에서 로그인 유저 idx에 해당하는 채팅방 정보를 삭제한다.
				chatController.enterCheckMap.remove(message.getIdx());
			}

			// 채팅방 유저 정보 Map에 로그인 유저 idx에 해당하는 채팅방 유저 정보 List가 존재하는지 체크한다.
			// 채팅방 유저 정보 List가 존재하는 경우
			if ( chatRoomMap.get(message.getIdx()) != null ) {
				// 채팅방 유저 정보 Map에서 로그인 유저 idx에 해당하는 채팅방 유저 정보 List를 삭제한다.
				chatRoomMap.remove(message.getIdx());
			}

			// 첫 메시지 체크용 Map에 첫 메시지 체크 값이 존재하는지 체크한다.
			// 첫 메시지 체크 값이 존재하지 않는 경우
			if ( messageCheck.get(message.getIdx()) != null ) {
				// 첫 메시지 체크용 Map에서 로그인 유저 idx에 해당하는 첫 메시지 체크 값을 삭제한다.
				messageCheck.remove(message.getIdx());
			}

			// 로그인 유저 idx를 키로 사용하고, 채팅방 아이디를 값으로 사용하여, 퇴장 체크용 Map에 추가한다.
			chatController.exitCheckMap.put(message.getIdx(), message.getId());

			// SimpMessagingTemplate를 통해 해당 path를 SUBSCRIBE하는 Client에게 파라미터로 받아온 메시지 DTO를 다시 전달한다.
			// path : StompWebSocketConfig에서 설정한 enableSimpleBroker와 DTO를 전달할 경로와 파라미터로 받아온 메시지 DTO 중 채팅방 아이디가 병합된다.
			// "/sub" + "/chat/" + id = "/sub/chat/1"
			template.convertAndSend("/sub/chat/" + message.getId(), message);
		}
	}
}
