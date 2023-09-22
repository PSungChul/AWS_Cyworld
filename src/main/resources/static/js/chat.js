let videoBtnArea = document.getElementById("videoBtnArea"); // 영상 통화 버튼 구역
let chatArea = document.getElementById("chatArea"); // 채팅 구역
let message = document.getElementById("message"); // 메시지 작성란
//////////////////////////////////////////////////// 소켓 구역 ////////////////////////////////////////////////////
// 1. SockJS를 생성한다. (StompWebSocketConfig에서 설정한 Endpoint와 동일하게 맞춰준다.)
let sockJs = new SockJS("/ws/chat");
// 2. 생성된 SockJS를 Stomp에 전달한다.
let stomp = Stomp.over(sockJs);
// 2-1. Stomp가 생성되면 Stomp 디버그 출력을 막는다.
stomp.debug = null;

// 3. connect가 이뤄지면 실행한다.
stomp.connect({}, function () {
    // 5. subscribe(path, callback)으로 메시지를 받을 수 있다.
    //    StompChatController에서 SimpMessagingTemplate를 통해 전달한 DTO를 여기서 콜백 메소드 파라미터로 전달 받는다.
    stomp.subscribe("/sub/chat/" + idx, function (chat) {
        // 6. JSON형식으로 넘어온 DTO를 JavaScript형식으로 변환한다.
        //    JSON.parse(변환 대상) - JSON 문자열을 JavaScript 값이나 객체로 변환한다.
        let chatMessage = JSON.parse(chat.body);
        // 6-1. 변환된 DTO를 사용하기 편하게 각각 변수에 나눠놓는다.
        let chatType = chatMessage.type; // 메시지 타입
        let chatId = chatMessage.id; // 채팅방 아이디
        let chatUnreadStatus = chatMessage.unreadStatus; // 안 읽은 메시지 수
        let chatIdx = chatMessage.idx; // 전송자 idx
        let sender = chatMessage.sender; // 전송자 이름
        let chatUserIdx = chatMessage.userIdx; // 상대 유저 idx
        let userSender = chatMessage.userSender; // 상대 유저 이름
        let chatUserEmail = chatMessage.userEmail; // 상대 유저 이메일
        let chatUserMainPhoto = chatMessage.userMainPhoto; // 상대 유저 메인 사진
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 7. 메시지 타입 값에 따라 나눈다.
        /////////////////////////////////////////////////// 채팅방 상태 ///////////////////////////////////////////////////
        // 7-1 메시지 타입이 "status"인 경우
        if ( chatType == "status" ) {
            // 검색 체크 값으로 검색 중인지 체크한다.
            // 검색 중이지 않은 경우
            if ( searchCheck == 0 ) {
                // 채팅방 목록 구역을 가져온다.
                let memberInfo = document.querySelector(".memberInfo");

                // 채팅방 아이디에 해당하는 채팅방 요소를 가져와 존재하는지 체크한다.
                let chatRoom = document.getElementsByName(chatId)[0];
                // 가져온 채팅방 요소가 존재하는 경우
                if ( chatRoom != null ) {
                    // 가져온 채팅방 요소를 삭제한다.
                    chatRoom.remove();
                }

                // input 태그를 생성한다.
                let inputTag = document.createElement("input");
                // input 태그의 class를 설정한다.
                inputTag.setAttribute("class", "userMainPhoto");
                // input 태그의 id를 설정한다.
                inputTag.setAttribute("id", chatId);
                // input 태그의 타입을 설정한다.
                inputTag.type = "image";
                // 상대 유저 메인 사진이 존재하는지 체크한다.
                // 메인 사진이 존재하지 않는 경우
                if ( chatUserMainPhoto == "noImage" ) {
                    // input 태그의 src를 기본 메인 사진으로 설정한다.
                    inputTag.src = "/images/noImage.jpeg";
                // 메인 사진이 존재하는 경우
                } else {
                    // input 태그의 src를 상대 유저 메인 사진으로 설정한다.
                    inputTag.src = "/filePath/profile/" + chatUserMainPhoto;
                }
                // input 태그의 값을 설정한다.
                inputTag.value = chatUserIdx;

                // 위쪽 span 태그를 생성한다.
                let topSpanTag = document.createElement("span");
                // 위쪽 span 태그의 class를 설정한다.
                topSpanTag.setAttribute("class", "userName");
                // 위쪽 span 태그의 내용을 작성한다.
                topSpanTag.innerHTML = userSender + " / " + chatUserEmail;

                // 아래쪽 span 태그를 생성한다.
                let bottomSpanTag = document.createElement("span");
                // 아래쪽 span 태그의 class를 설정한다.
                bottomSpanTag.setAttribute("class", "unreadStatus");
                // 아래쪽 span 태그의 내용을 작성한다.
                bottomSpanTag.innerHTML = chatUnreadStatus;

                // figure 태그를 생성한다.
                let figureTag = document.createElement("figure");
                // figure 태그에 input 태그를 전달한다.
                figureTag.appendChild(inputTag);
                // figure 태그에 위쪽 span 태그를 전달한다.
                figureTag.appendChild(topSpanTag);
                // figure 태그에 아래쪽 span 태그를 전달한다.
                figureTag.appendChild(bottomSpanTag);

                // div 태그를 생성한다.
                let divTag = document.createElement("div");
                // div 태그의 class를 설정한다.
                divTag.setAttribute("class", "chatRoomList");
                // div 태그의 name을 채팅방 아이디로 설정한다.
                divTag.setAttribute("name", chatId);
                // div 태그에 figure 태그를 전달한다.
                divTag.appendChild(figureTag);

                // 새로 생성한 채팅방 요소를 채팅방 목록 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                memberInfo.insertAdjacentHTML("afterbegin", divTag.outerHTML);
            }
        } // status
    }); // stomp
});