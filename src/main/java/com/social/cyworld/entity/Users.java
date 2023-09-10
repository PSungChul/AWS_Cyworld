package com.social.cyworld.entity;

import com.social.cyworld.dto.UserDTO;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "Users")
public class Users {
    private int _id; // 로그인 유저 idx
    private List<ChatRoomList> chatRooms; // 참여중인 채팅방 목록

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ChatRoomList {
        private String _id; // 채팅방 아이디
        private int idx; // 상대 유저 idx
        private String email; // 상대 유저 이메일
        private String name; // 상대 유저 이름
        private String mainPhoto; // 상대 유저 메인 사진
        private int unreadStatus; // 안 읽은 메시지 수

        public static ChatRoomList toEntity(String id, UserDTO userDTO) {
            return ChatRoomList.builder()
                    ._id(id)
                    .idx(userDTO.getIdx())
                    .email(userDTO.getEmail())
                    .name(userDTO.getName())
                    .mainPhoto(userDTO.getMainPhoto())
                    .build();
        }

        public static ChatRoomList toEntity(HashMap<String, Object> user) {
            return ChatRoomList.builder()
                    ._id((String) user.get("id"))
                    .idx((int) user.get("idx"))
                    .email((String) user.get("email"))
                    .name((String) user.get("name"))
                    .mainPhoto((String) user.get("mainPhoto"))
                    .build();
        }
    }
}
