package com.social.cyworld.dto;

import com.social.cyworld.entity.Users;
import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatMessageDTO {
    private int participants; // 참여자 수
    private String type; // 메시지 타입
    private String id; // 채팅방 아이디
    private String content; // 메세지
    private String status; // 읽음 / 안 읽음 상태
    private int unreadStatus; // 안 읽은 메시지 수

    // 로그인 유저 정보
    private int idx; // 유저 idx
    private String sender; // 유저 이름
    private String email; // 유저 이메일
    private String mainPhoto; // 프로필 사진

    // 상대 유저 정보
    private int userIdx; // 유저 idx
    private String userSender; // 상대 유저 이름
    private String userEmail; // 유저 이메일
    private String userMainPhoto; // 프로필 사진
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ChatMessageDTO toChatMessage(HashMap<String, Object> user) {
        return ChatMessageDTO.builder()
                .participants(participants)
                .type(type)
                .content(content)
                .status(status)
                .unreadStatus(unreadStatus)
                .id(id)
                .idx((int) user.get("idx"))
                .sender((String) user.get("name"))
                .mainPhoto((String) user.get("mainPhoto"))
                .build();
    }
}
