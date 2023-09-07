package com.social.cyworld.entity;

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
