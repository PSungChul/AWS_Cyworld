package com.social.cyworld.util;

import com.mongodb.BasicDBObject;
import com.social.cyworld.entity.ChatRooms;
import com.social.cyworld.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MongoUtil {
    @Autowired
    MongoTemplate mongoTemplate;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Users
    // 유저 idx에 해당하는 채팅방을 모두 조회
    public List<Users.ChatRoomList> findAllChatRoomList(int idx) {
        // 조회 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(idx));
        // 조회 쿼리를 실행한다.
        Users user = mongoTemplate.findOne(query, Users.class);

        // 유저 idx에 해당하는 유저 정보가 존재하지 않는 경우
        if ( user == null ) {
            // 유저 정보 Entity를 생성한다.
            user = new Users();
            // 유저 정보 _id에 로그인 유저 idx를 setter를 통해 전달한다.
            user.set_id(idx);
            // 유저 정보 채팅방 리스트에 빈 리스트로 초기화하여 setter를 통해 전달한다.
            user.setChatRooms(new ArrayList<>());
            // 전달받은 유저 정보를 저장한다.
            mongoTemplate.save(user, "Users");
            // null을 반환한다.
            return null;
        // 유저 idx에 해당하는 유저 정보가 존재하는 경우
        } else {
            // 채팅방 리스트를 반환한다.
            return user.getChatRooms();
        }
    }

    // 로그인 유저 idx와 상대 유저 idx에 해당하는 채팅방 조회
    public Users findChatRoom(int loginIdx, int idx) {
        // 조회 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(loginIdx) // 로그인 유저 idx
                .and("chatRooms").elemMatch(
                        Criteria.where("idx").is(idx) // 상대 유저 idx
                ));
        // 조회 쿼리를 실행한다.
        Users user = mongoTemplate.findOne(query, Users.class);

        // 로그인 유저 idx와 상대 유저 idx에 해당하는 채팅방 정보가 존재하지 않는 경우
        if ( user == null ) {
            // null을 반환한다.
            return null;
        // 로그인 유저 idx와 상대 유저 idx에 해당하는 채팅방 정보가 존재하는 경우
        } else {
            // 채팅방 정보를 반환한다.
            return user;
        }
    }

    // 채팅방 정보 삭제 후 저장 - 채팅방 정보가 존재하는 경우 안 읽은 메시지 수 0으로 갱신
    public void enterDeleteAndInsertChatRoom(int loginIdx, Users.ChatRoomList chatRoom) {
        // 조회 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(loginIdx) // 로그인 유저 idx
                .and("chatRooms").elemMatch(
                        Criteria.where("idx").is(chatRoom.getIdx()) // 상대 유저 idx
                ));
        // 조회 쿼리를 실행한다.
        Users user = mongoTemplate.findOne(query, Users.class);

        // 로그인 유저 idx와 상대 유저 idx에 해당하는 채팅방 정보가 존재하는 경우
        if ( user != null ) {
            // 업데이트 쿼리를 생성한다.
            query = new Query(Criteria.where("_id").is(loginIdx));

            // 기존 채팅방 정보를 삭제할 내용을 정의한다.
            Update update = new Update().pull("chatRooms", new BasicDBObject("_id", chatRoom.get_id()));
            // 기존 채팅방 정보를 삭제한다.
            mongoTemplate.updateFirst(query, update, Users.class);

            chatRoom.setUnreadStatus(0);

            // 새로운 채팅방 정보를 추가할 내용을 정의한다.
            update = new Update().push("chatRooms", chatRoom);
            // 새로운 채팅방 정보를 추가한다.
            mongoTemplate.upsert(query, update, Users.class);
        }
    }

    // 채팅방 정보 삭제 후 저장
    public void deleteAndInsertChatRoom(int loginIdx, Users.ChatRoomList chatRoom) {
        // 업데이트 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(loginIdx));

        // 기존 채팅방 정보를 삭제할 내용을 정의한다.
        Update update = new Update().pull("chatRooms", new BasicDBObject("_id", chatRoom.get_id()));
        // 기존 채팅방 정보를 삭제한다.
        mongoTemplate.updateFirst(query, update, Users.class);

        chatRoom.setUnreadStatus(0);

        // 새로운 채팅방 정보를 추가할 내용을 정의한다.
        update = new Update().push("chatRooms", chatRoom);
        // 새로운 채팅방 정보를 추가한다.
        mongoTemplate.upsert(query, update, Users.class);
    }

    // 채팅방 정보 삭제 후 저장 - 상대 채팅방 정보에 안 읽은 메시지 수 추가
    public void deleteAndInsertChatRoom(int loginIdx, Users.ChatRoomList chatRoom, int count) {
        // 업데이트 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(loginIdx));

        // 기존 채팅방 정보를 삭제할 내용을 정의한다.
        Update update = new Update().pull("chatRooms", new BasicDBObject("_id", chatRoom.get_id()));
        // 기존 채팅방 정보를 삭제한다.
        mongoTemplate.updateFirst(query, update, Users.class);

        // 채팅방 정보 중 안 읽은 메시지 수에 파라미터로 가져온 안 읽은 메시지 수를 setter를 통해 전달한다.
        chatRoom.setUnreadStatus(count);

        // 새로운 채팅방 정보를 추가할 내용을 정의한다.
        update = new Update().push("chatRooms", chatRoom);
        // 새로운 채팅방 정보를 추가한다.
        mongoTemplate.upsert(query, update, Users.class);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////ChatRooms
    // 채팅방 아이디에 해당하는 메시지를 모두 조회 + 채팅방 아이디에 해당하는 메시지 중 작성자가 상대 유저 idx에 해당하고 상태가 1인 메시지를 찾아 상태를 0으로 변경
    public List<ChatRooms.ChatMessageList> findAllChatMessageList(String id, int idx) {
        // 조회 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(id));
        // 조회 쿼리를 실행한다.
        ChatRooms chatRoom = mongoTemplate.findOne(query, ChatRooms.class);

        // 채팅방 아이디에 해당하는 채팅방 정보가 존재하지 않는 경우
        if ( chatRoom == null ) {
            // null을 반환한다.
            return null;
        // 채팅방 아이디에 해당하는 채팅방 정보가 존재하는 경우
        } else {
            // 조회한 채팅방 정보 중 메시지 리스트를 가져온다.
            List<ChatRooms.ChatMessageList> messages = chatRoom.getMessages();

            // 가져온 메시지를 리스트를 마지막부터 시작해서 순회하면서 조건에 해당하는 메시지의 상태를 변경한다.
            for ( int i = messages.size() - 1; i >= 0; i-- ) {
                // 메시지 전송 유저 idx가 상대 유저 idx이면서, 메시지 상태가 안 읽음 상태를 의미하는 1인 경우
                if ( messages.get(i).getIdx() == idx && "1".equals(messages.get(i).getStatus()) ) {
                    // 해당 메시지 상태에 읽음 상태를 의미하는 0을 setter를 통해 전달한다.
                    messages.get(i).setStatus("0");
                    // 바로 다음 for문을 실행한다.
                    continue;
                }
                // 메시지 전송 유저 idx가 상대 유저 idx이면서, 메시지 상태가 읽음 상태를 의미하는 0인 경우
                if ( messages.get(i).getIdx() == idx && "1".equals(messages.get(i).getStatus()) ) {
                    // for문을 벗어난다.
                    break;
                }
            }

            // 조회한 채팅방 정보 중 메시지 리스트에 상태를 변경한 메시지 리스트를 setter를 통해 전달한다.
            chatRoom.setMessages(messages);

            // 전달받은 채팅방 정보로 갱신한다.
            mongoTemplate.save(chatRoom);

            // 상태를 변경한 메시지 리스트를 반환한다.
            return chatRoom.getMessages();
        }
    }

    // 안 읽은 메시지 수 조회
    public Integer countChatStatus(String id, int idx) {
        // 조회 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(id));
        // 조회 쿼리를 실행한다.
        ChatRooms chatRoom = mongoTemplate.findOne(query, ChatRooms.class);

        // 채팅방 아이디에 해당하는 채팅방 정보가 존재하지 않는 경우
        if ( chatRoom == null ) {
            // null을 반환한다.
            return null;
            // 채팅방 아이디에 해당하는 채팅방 정보가 존재하는 경우
        } else {
            // 조회한 채팅방 정보 중 메시지 리스트를 가져온다.
            List<ChatRooms.ChatMessageList> messages = chatRoom.getMessages();

            // 안 읽은 메시지 수
            int count = 0;

            // 가져온 메시지를 리스트를 마지막부터 시작해서 순회하면서 조건에 해당하는 안 읽은 메시지 수를 구한다.
            for ( int i = messages.size() - 1; i >= 0; i-- ) {
                // 메시지 전송 유저 idx가 로그인 유저 idx이면서, 메시지 상태가 안 읽음 상태를 의미하는 1인 경우
                if ( messages.get(i).getIdx() == idx && "1".equals(messages.get(i).getStatus()) ) {
                    // 안 읽은 메시지 수를 1 증가시긴다.
                    count++;
                    // 바로 다음 for문을 실행한다.
                    continue;
                }
                // 메시지 전송 유저 idx가 로그인 유저 idx이면서, 메시지 상태가 읽음 상태를 의미하는 0인 경우
                if ( messages.get(i).getIdx() == idx && "1".equals(messages.get(i).getStatus()) ) {
                    // for문을 벗어난다.
                    break;
                }
            }

            return count;
        }
    }

    // 채팅방 정보 첫 저장
    public void insertFirstChatMessage(String id) {
        // 조회 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(id));
        // 조회 쿼리를 실행한다.
        ChatRooms chatRoom = mongoTemplate.findOne(query, ChatRooms.class);

        // 채팅방 아이디에 해당하는 채팅방 정보가 존재하지 않는 경우
        if ( chatRoom == null ) {
            // 채팅방 정보 Entity를 생성한다.
            chatRoom = new ChatRooms();
            // 유저 정보 _id에 로그인 유저 idx를 setter를 통해 전달한다.
            chatRoom.set_id(id);
            // 유저 정보 채팅방 리스트에 빈 리스트로 초기화하여 setter를 통해 전달한다.
            chatRoom.setMessages(new ArrayList<>());
            // 전달받은 채팅방 정보를 저장한다.
            mongoTemplate.save(chatRoom, "ChatRooms");
        }
    }

    // 메시지 저장
    public void insertChatMessage(String id, ChatRooms.ChatMessageList chatMessage) {
        // 업데이트 쿼리를 생성한다.
        Query query = new Query(Criteria.where("_id").is(id));
        // 업데이트할 내용을 정의한다.
        Update update = new Update().push("messages", chatMessage);
        // 업데이트 쿼리를 실행한다.
        mongoTemplate.updateFirst(query, update, ChatRooms.class);
    }
}