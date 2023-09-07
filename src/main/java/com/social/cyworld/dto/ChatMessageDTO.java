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
    private String type; // 메시지 타입
    private String id; // 방 아이디

    // 로그인 유저 정보
    private int idx; // 유저 idx
    private String email; // 유저 이메일
    private String sender; // 전송자
    private String mainPhoto; // 프로필 사진
    private String content; // 메세지
    private String status; // 읽음 / 안 읽음 상태
    private int participants; // 참여자 수

    // 상대 유저 정보
    private int userIdx; // 유저 idx
    private String userEmail; // 유저 이메일
    private String userSender; // 전송자
    private String userMainPhoto; // 프로필 사진
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Users.ChatRoomList toChatRoom() {
        return Users.ChatRoomList.builder()
                ._id(id)
                .idx(idx)
                .email(email)
                .name(sender)
                .mainPhoto(mainPhoto)
                .build();
    }

    public ChatMessageDTO toChatMessage(HashMap<String, Object> user) {
        return ChatMessageDTO.builder()
                .type(type)
                .id(id)
                .idx((int) user.get("idx"))
                .sender((String) user.get("name"))
                .mainPhoto((String) user.get("mainPhoto"))
                .content(content)
                .status(status)
                .participants(participants)
                .build();
    }
}
