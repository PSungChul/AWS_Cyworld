package com.social.cyworld.entity;

import com.social.cyworld.dto.ChatMessageDTO;
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
@Document(collection = "ChatRooms")
public class ChatRooms {
    private String _id; // 채팅방 고유 아이디
    private List<ChatMessageList> messages; // 채팅방 메시지 목록

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ChatMessageList {
        private String type; // 메시지 타입
        private int idx; // 유저 idx
        private String sender; // 전송자
        private String mainPhoto; // 메인 사진
        private String content; // 메시지 내용
        private String status; // 메시지 읽음 / 안 읽음 상태

        // 메시지 타입이 메시지인 경우
        public static ChatMessageList toMessage(HashMap<String, Object> user, ChatMessageDTO message) {
            return ChatMessageList.builder()
                    .type("msg")
                    .idx((int) user.get("idx"))
                    .sender((String) user.get("name"))
                    .mainPhoto((String) user.get("mainPhoto"))
                    .content(message.getContent())
                    .status(message.getStatus())
                    .build();
        }

        // 메시지 타입이 녹음인 경우
        public static ChatMessageList toRecord(HashMap<String, Object> user, String recordFile, String status) {
            return ChatMessageList.builder()
                    .type("rec")
                    .idx((int) user.get("idx"))
                    .sender((String) user.get("name"))
                    .mainPhoto((String) user.get("mainPhoto"))
                    .content(recordFile)
                    .status(status)
                    .build();
        }
    }
}
