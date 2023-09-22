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
    stomp.subscribe("/sub/chat/room/" + id, function (chat) {
        // 5-1. 채팅에 필요한 것들
        let msg = ""; // 메시지 코드가 작성될 변수
        // 6. 5에서 전달받은 메시지의 headers에서 type 값이 "record"인지 체크한다.
        // 6-1. type 값이 "record"인 경우
        if ( chat.headers.type == "record" ) {
            //////////////////////////////////////////전달 받은 녹음 올리기//////////////////////////////////////////
            // 7. document.createElement()를 사용하여, 전달 받은 녹음된 오디오 메시지를 코드로 작성해 채팅 구역에 올린다.
            // document.createElement() - 지정한 태그 이름을 가진 HTML 요소를 생성하는 메소드이다.
            //                            이 메소드를 사용하면 동적으로 HTML 요소를 생성할 수 있다.

            // 1. audio(오디오) 태그를 생성한다.
            let audioTag = document.createElement("audio");
            // 1-1. 5에서 전달 받은 메시지의 body에서 녹음된 오디오 데이터를 가져와 audio(오디오) 태그의 소스로 전달한다.
            audioTag.src = chat.body;
            // 1-2. audio(오디오) 태그의 컨트롤러를 설정한다.
            audioTag.controls = true;

            if ( chat.headers.idx == idx ) {
                // 2. span 태그를 생성한다.
                let spanTag = document.createElement("span");
                // 2-1. span 태그의 class를 설정한다.
                spanTag.setAttribute("class", "meStatus")
                // document.createTextNode() - 지정한 텍스트를 포함하는 새로운 Text 노드를 생성하는 메소드이다.
                //                             이 메소드를 사용하여 동적으로 HTML 요소의 텍스트를 업데이트할 수 있다.
                //                             이 메소드는 document.createElement()와 함께 사용하여 동적으로 HTML 요소를 생성하는 데 사용된다.
                // 2-2. span 태그에 작성할 내용을 TextNode로 생성한다.
                let spanContent = document.createTextNode(chat.headers.status);
                // 2-3. span 태그에 TextNode로 생성한 내용을 전달한다.
                spanTag.appendChild(spanContent);

                // 3. b 태그를 생성한다.
                let bTag = document.createElement("b");
                // 3-1. b 태그의 class를 설정한다.
                bTag.setAttribute("class", "chatMsg");
                // 3-2. b 태그에 audio(오디오) 태그를 전달한다.
                bTag.appendChild(audioTag);

                // 4. div 태그를 생성한다.
                let divTag = document.createElement("div");
                // 4-1. div 태그의 class를 설정한다.
                divTag.setAttribute("class", "chat me");
                // 4-2. div 태그에 span 태그를 전달한다.
                divTag.appendChild(spanTag);
                // 4-3. div 태그에 b 태그를 전달한다.
                divTag.appendChild(bTag);

                // 5. 채팅 구역에 div 태그를 올린다.
                chatArea.appendChild(divTag);

                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
            } else {
                // 2. img 태그를 생성한다.
                let imgTag = document.createElement("img");
                // 메인 사진이 존재하지 않는 경우
                if ( chat.headers.mainPhoto == "noImage" ) {
                    // 2-1. img 태그의 class를 설정한다.
                    imgTag.setAttribute("class", "chatImg");
                    // 2-2. img 태그의 이미지 소스를 설정한다.
                    imgTag.src = "/images/noImage.jpeg";
                // 메인 사진이 존재하는 경우
                } else {
                    // 2-1. img 태그의 class를 설정한다.
                    imgTag.setAttribute("class", "chatImg");
                    // 2-2. img 태그의 이미지 소스를 설정한다.
                    imgTag.src = "/filePath/profile/" + chat.headers.mainPhoto;
                }

                // 3. b 태그를 생성한다.
                let bTag = document.createElement("b");
                // 3-1. b 태그의 class를 설정한다.
                bTag.setAttribute("class", "chatMsg");
                // document.createTextNode() - 지정한 텍스트를 포함하는 새로운 Text 노드를 생성하는 메소드이다.
                //                             이 메소드를 사용하여 동적으로 HTML 요소의 텍스트를 업데이트할 수 있다.
                //                             이 메소드는 document.createElement()와 함께 사용하여 동적으로 HTML 요소를 생성하는 데 사용된다.
                // 3-2. b 태그에 작성할 내용을 TextNode로 생성한다.
                let bContent = document.createTextNode(chat.headers.sender + ": ");
                // 3-3. b 태그에 TextNode로 생성한 내용을 전달한다.
                bTag.appendChild(bContent);
                // 3-4. b 태그에 audio(오디오) 태그를 전달한다.
                bTag.appendChild(audioTag);

                // 4. span 태그를 생성한다.
                let spanTag = document.createElement("span");
                // 4-1. span 태그의 class를 설정한다.
                spanTag.setAttribute("class", "youStatus")
                // document.createTextNode() - 지정한 텍스트를 포함하는 새로운 Text 노드를 생성하는 메소드이다.
                //                             이 메소드를 사용하여 동적으로 HTML 요소의 텍스트를 업데이트할 수 있다.
                //                             이 메소드는 document.createElement()와 함께 사용하여 동적으로 HTML 요소를 생성하는 데 사용된다.
                // 4-2. span 태그에 작성할 내용을 TextNode로 생성한다.
                let spanContent = document.createTextNode(chat.headers.status);
                // 4-3. span 태그에 TextNode로 생성한 내용을 전달한다.
                spanTag.appendChild(spanContent);

                // 5. div 태그를 생성한다.
                let divTag = document.createElement("div");
                // 5-1. div 태그의 class를 설정한다.
                divTag.setAttribute("class", "chat you");
                // 5-2. div 태그에 img 태그를 전달한다.
                divTag.appendChild(imgTag);
                // 5-3. div 태그에 b 태그를 전달한다.
                divTag.appendChild(bTag);
                // 5-4. div 태그에 span 태그를 전달한다.
                divTag.appendChild(spanTag);

                // 6. 채팅 구역에 div 태그를 올린다.
                chatArea.appendChild(divTag);

                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
            }
            ////////////////////////////////////////// 전달 받은 녹음 재생 //////////////////////////////////////////
            // 8. 채팅 구역에 올라가 있는 audio(오디오) 태그를 모두 선택하여 audioTags 변수에 NodeList 형태로 저장한다.
            // querySelectorAll - HTML 문서 내에서 특정 CSS 선택자에 해당하는 요소들을 찾아 NodeList 형태로 반환하는 메소드이다.
            // NodeList - DOM에서 사용되는 객체로, 여러 개의 노드(HTML 요소, 속성, 텍스트 등)가 포함될 수 있는 리스트 형태의 객체를 나타낸다.
            //            일반적으로 querySelectorAll 메소드로 선택된 요소들을 반환하는데 사용된다.
            //            NodeList 객체는 배열과 유사하게 인덱스를 사용하여 접근할 수 있으며, length 속성을 통해 그 길이를 알 수 있다.
            //            하지만 NodeList는 배열이 아니므로, 배열 메소드를 사용할 수 없다.
            let audioTags = document.querySelectorAll("#chatArea audio");
            // forEach 메소드를 사용하여 audioTags 변수에 저장되어 있는 NodeList의 각 요소에 대하여 이벤트 리스너를 등록한다.
            audioTags.forEach(audio => {
                // forEach 메소드를 사용하여 audioTags 변수에 저장되어 있는 NodeList에서 가져온 각 audio(오디오) 태그마다,
                // "ended" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                // ended - 오디오나 비디오 등의 미디어가 재생이 끝났을 때 발생하는 이벤트이다.
                audio.addEventListener("ended", () => {
                    // 각 audio(오디오) 태그의 재생이 종료되면, audio(오디오) 태그에 작성되어 있는 소스를 그대로 재작성한다.
                    // 이는 오디오 재생이 끝나고 다시 재생할 때, 오디오 소스가 초기화되도록 하는 역할을 한다.
                    // 이렇게 하는 이유는, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터를 온전히 다시 재생하기 위해서이다.
                    // 이렇게 하지 않으면, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터중 마지막 오디오 데이터만 재생된다.
                    // 하나의 오디오 데이터는 문제가 없지만, 병합된 여러개의 오디오 데이터는 이런 문제가 발생해 임시방편으로 찾아낸 방법이다.
                    audio.src = audio.src;
                }); // addEventListener
            }); // forEach
        // 6-2. type 값이 "record"가 아닌 경우
        } else {
            // 7. JSON형식으로 넘어온 DTO를 JavaScript형식으로 변환한다.
            //    JSON.parse(변환 대상) - JSON 문자열을 JavaScript 값이나 객체로 변환한다.
            let chatMessage = JSON.parse(chat.body);
            // 7-1. 변환된 DTO를 사용하기 편하게 각각 변수에 나눠놓는다.
            let chatParticipants = chatMessage.participants; // 참여중인 인원
            let chatType = chatMessage.type; // 메시지 타입
            let chatId = chatMessage.id; // 채팅방 아이디
            let content = chatMessage.content; // 메시지 내용
            let status = chatMessage.status; // 읽음 / 안 읽음 상태
            let chatUnreadStatus = chatMessage.unreadStatus; // 안 읽은 메시지 수
            let chatIdx = chatMessage.idx; // 전송자 idx
            let sender = chatMessage.sender; // 전송자 이름
            let chatMainPhoto = chatMessage.mainPhoto; // 전송자 프로필 사진
            let chatUserIdx = chatMessage.userIdx; // 수신자 idx
            let userSender = chatMessage.userSender; // 수신자 이름
            let chatUserEmail = chatMessage.userEmail; // 수신자 이메일
            let chatUserMainPhoto = chatMessage.userMainPhoto; // 수신자 메인 사진
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 8. 참여중인 인원이 바뀌는 경우
            if ( participants != chatParticipants ) {
                // 전역 변수에 선언되어 있는 참여중인 인원을 새로 받아온 참여중인 인원으로 변경한다.
                participants = chatParticipants;
                if ( participants == 2 ) {
                    let meStatus = document.getElementsByClassName("meStatus");

                    for ( let i = meStatus.length - 1; i >= 0; i-- ) {
                        if ( meStatus[i].textContent == 1 ) {
                            meStatus[i].textContent = "";
                            continue;
                        }
                        break;

                    }
                }
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 9. 메시지 타입 값에 따라 나눈다.
            //////////////////////////////////////////////// 입장 or 재입장 ////////////////////////////////////////////////
            // 9-1. 메시지 타입이 "enter" or "reEnter"인 경우
            if ( chatType == "enter" || chatType == "reEnter" ) {
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;

                // 메시지 내용이 존재하는지 체크한다.
                // 메시지 내용이 존재하는 경우 - 영상 통화 신청 중
                if ( content == "apply" ) {
                    // 전송자 idx와 로그인 유저 idx가 같은지 체크한다.
                    // 전송자 idx와 로그인 유저가 idx가 다른 경우
                    if ( chatIdx != idx ) {
                        // 영상 통화 버튼 구역을 비운다.
                        videoBtnArea.innerHTML = "";

                        // 수락 input 태그를 생성한다.
                        let acceptInputTag = document.createElement("input");
                        // 수락 input 태그의 class를 설정한다.
                        acceptInputTag.setAttribute("class", "acceptBtn");
                        // 수락 input 태그의 타입을 설정한다.
                        acceptInputTag.type = "button";
                        // 수락 input 태그의 값을 설정한다.
                        acceptInputTag.value = "수락";
                        // 수락 input 태그의 onclick을 설정한다.
                        acceptInputTag.onclick = function() {
                            accept();
                        };

                        // 거절 input 태그를 생성한다.
                        let refuseInputTag = document.createElement("input");
                        // 거절 input 태그의 class를 설정한다.
                        refuseInputTag.setAttribute("class", "refuseBtn");
                        // 거절 input 태그의 타입을 설정한다.
                        refuseInputTag.type = "button";
                        // 거절 input 태그의 값을 설정한다.
                        refuseInputTag.value = "거절";
                        // 거절 input 태그의 onclick을 설정한다.
                        refuseInputTag.onclick = function() {
                            refuse();
                        };

                        // 영상 통화 버튼 구역에 수락 input 태그를 올린다.
                        videoBtnArea.appendChild(acceptInputTag);
                        // 영상 통화 버튼 구역에 거절 input 태그를 올린다.
                        videoBtnArea.appendChild(refuseInputTag);
                    // 전송자 idx와 로그인 유저가 idx가 다른 경우
                    } else {
                        // 영상 통화 버튼 구역을 비운다.
                        videoBtnArea.innerHTML = "";

                        // 취소 input 태그를 생성한다.
                        let cancelInputTag = document.createElement("input");
                        // 취소 input 태그의 class를 설정한다.
                        cancelInputTag.setAttribute("class", "videoBtn");
                        // 취소 input 태그의 id를 설정한다.
                        cancelInputTag.setAttribute("id", "cancelBtn");
                        // 취소 input 태그의 타입을 설정한다.
                        cancelInputTag.type = "button";
                        // 취소 input 태그의 값을 설정한다.
                        cancelInputTag.value = "통화 취소";
                        // 취소 input 태그의 onclick을 설정한다.
                        cancelInputTag.onclick = function() {
                            refuse();
                        };

                        // 영상 통화 버튼 구역에 취소 input 태그를 올린다.
                        videoBtnArea.appendChild(cancelInputTag);
                    }
                }
            } // enter
            ////////////////////////////////////////////////// 영상 통화 //////////////////////////////////////////////////
            // 9-2. 메시지 타입이 "video"인 경우
            if ( chatType == "video" ) {
                // 메시지 내용을 체크한다.
                // 메시지 내용이 "apply"인 경우 - 신청
                if ( content == "apply" ) {
                    // 전송자 idx와 로그인 유저 idx가 같은지 체크한다.
                    // 전송자 idx와 로그인 유저가 idx가 다른 경우
                    if ( chatIdx != idx ) {
                        // 영상 통화 버튼 구역을 비운다.
                        videoBtnArea.innerHTML = "";

                        // 수락 input 태그를 생성한다.
                        let acceptInputTag = document.createElement("input");
                        // 수락 input 태그의 class를 설정한다.
                        acceptInputTag.setAttribute("class", "acceptBtn");
                        // 수락 input 태그의 타입을 설정한다.
                        acceptInputTag.type = "button";
                        // 수락 input 태그의 값을 설정한다.
                        acceptInputTag.value = "수락";
                        // 수락 input 태그의 onclick을 설정한다.
                        acceptInputTag.onclick = function() {
                            accept();
                        };

                        // 거절 input 태그를 생성한다.
                        let refuseInputTag = document.createElement("input");
                        // 거절 input 태그의 class를 설정한다.
                        refuseInputTag.setAttribute("class", "refuseBtn");
                        // 거절 input 태그의 타입을 설정한다.
                        refuseInputTag.type = "button";
                        // 거절 input 태그의 값을 설정한다.
                        refuseInputTag.value = "거절";
                        // 거절 input 태그의 onclick을 설정한다.
                        refuseInputTag.onclick = function() {
                            refuse();
                        };

                        // 영상 통화 버튼 구역에 수락 input 태그를 올린다.
                        videoBtnArea.appendChild(acceptInputTag);
                        // 영상 통화 버튼 구역에 거절 input 태그를 올린다.
                        videoBtnArea.appendChild(refuseInputTag);
                    }
                }
                // 메시지 내용이 "accept"인 경우 - 수락
                if ( content == "accept" ) {
                    // 전송자 idx와 로그인 유저가 idx랑 다른 경우
                    if ( chatIdx != idx ) {
                        // webRtc.js에 있는 영상 통화 시작 메소드를 실행한다.
                        startVideo();
                    }
                }
                // 메시지 내용이 "refuse" or "timeout"인 경우 - 거절 or 시간 초과
                if ( content == "refuse" || content == "timeout" ) {
                    // 영상 통화 버튼 구역을 비운다.
                    videoBtnArea.innerHTML = "";

                    // 통화 input 태그를 생성한다.
                    let videoInputTag = document.createElement("input");
                    // 통화 input 태그의 class를 설정한다.
                    videoInputTag.setAttribute("class", "videoBtn");
                    // 통화 input 태그의 타입을 설정한다.
                    videoInputTag.type = "button";
                    // 통화 input 태그의 값을 설정한다.
                    videoInputTag.value = "영상 통화";
                    // 통화 input 태그의 onclick을 설정한다.
                    videoInputTag.onclick = function() {
                        video();
                    };

                    // 영상 통화 버튼 구역에 통화 input 태그를 올린다.
                    videoBtnArea.appendChild(videoInputTag);
                }
            }
            //////////////////////////////////////////////////// 채팅 ////////////////////////////////////////////////////
            // 9-3. 메시지 타입이 "chat"인 경우
            if ( chatType == "chat" ) {
                // 전송자 idx와 로그인 유저 idx가 같은지 체크한다.
                // 전송자 idx와 로그인 유저가 idx가 같은 경우
                if ( chatIdx == idx ) {
                    // 메시지 코드를 작성한다.
                    msg = '<div class="chat me">';
                    msg += '<span class="meStatus">' + status + '</span>'
                    msg += '<b class="chatMsg">' + content + '</b>';
                    msg += '</div>'
                    // 작성한 메시지 코드를 채팅 구역에 올린다.
                    // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                    // position에는 총 4가지의 옵션이 있다.
                    // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                    // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                    // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                    // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                    chatArea.insertAdjacentHTML("beforeend", msg);
                    // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                    chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
                // 전송자 idx와 로그인 유저가 idx가 다른 경우
                } else {
                    // 메시지 코드를 작성한다.
                    msg = '<div class="chat you">';
                    if ( chatMainPhoto == "noImage" ) {
                        msg += '<img class="chatImg" src="/images/' + chatMainPhoto + '.jpeg" alt="">';
                    } else {
                        msg += '<img class="chatImg" src="/filePath/profile/' + chatMainPhoto + '" alt="">';
                    }
                    msg += '<b class="chatMsg">' + sender + ': ' + content + '</b>';
                    msg += '<span class="youStatus">' + status + '</span>'
                    msg += '</div>'
                    // 작성한 메시지 코드를 채팅 구역에 올린다.
                    // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                    // position에는 총 4가지의 옵션이 있다.
                    // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                    // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                    // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                    // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                    chatArea.insertAdjacentHTML("beforeend", msg);
                    // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                    chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
                }
            } // chat
            ///////////////////////////////////////////////// 채팅방 상태 /////////////////////////////////////////////////
            // 9-4 메시지 타입이 "status"인 경우
            if ( chatType == "status" ) {
                // 전송자 idx와 로그인 유저 idx가 같은지 체크한다.
                // 전송자 idx와 로그인 유저가 idx가 같은 경우
                if ( chatIdx == idx ) {
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
                }
            } // status
        } // record
    }); // stomp
    ////////////////////////////////////////////////// 입장 구역 //////////////////////////////////////////////////
    // 입장 시작!! - 먼저 입장 체크 값을 이용하여 해당 유저가 첫 입장인지 재입장(새로고침)인지 체크한다.
    // 첫 입장일 경우 - 입장 체크 값이 존재하지 않는다.
    if ( entryCheck == null ) {
        // 이전 로컬 스토리지 값들을 제거한다.
        localStorage.clear();
        // 시작 시간과 메시지 전송 시간과 전송된 메시지 수와 메시지 전송 상태의 각 변수명을 키로 사용하고, 각 초기값을 지정해 값으로 사용하여 로컬 스토리지에 추가한다.
        localStorage.setItem("start", Date.now()); // Date.now()로 초기화
        localStorage.setItem("end", Date.now()); // Date.now()로 초기화
        localStorage.setItem("count", 0); // 0으로 초기화
        localStorage.setItem("sendMessage", "true"); // true로 초기화
        // 로컬 스토리지에 추가한 값들을 가져와 각 알맞는 변수에 전달한다.
        start = localStorage.getItem("start");
        end = localStorage.getItem("end");
        count = localStorage.getItem("count");
        sendMessage = localStorage.getItem("sendMessage");
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장할때 딱 한번만 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/chat/room/enter', {}, JSON.stringify({type: "enter", id: id, idx: idx, userIdx: userIdx}));
    } else {
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장 이후 모든 재입장(새로고침)은 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/chat/room/reenter', {}, JSON.stringify({type: "reEnter", id: id, idx: idx, userIdx: userIdx}));
    }
});
////////////////////////////////////////////////// 영상 통화 구역 //////////////////////////////////////////////////
// 영상 통화 신청 메소드
function video() {
    // 영상 통화 버튼 구역을 비운다.
    videoBtnArea.innerHTML = "";

    // 취소 input 태그를 생성한다.
    let cancelInputTag = document.createElement("input");
    // 취소 input 태그의 class를 설정한다.
    cancelInputTag.setAttribute("class", "videoBtn");
    // 취소 input 태그의 id를 설정한다.
    cancelInputTag.setAttribute("id", "cancelBtn");
    // 취소 input 태그의 타입을 설정한다.
    cancelInputTag.type = "button";
    // 취소 input 태그의 값을 설정한다.
    cancelInputTag.value = "통화 취소";
    // 취소 input 태그의 onclick을 설정한다.
    cancelInputTag.onclick = function() {
        refuse();
    };

    // 영상 통화 버튼 구역에 취소 input 태그를 올린다.
    videoBtnArea.appendChild(cancelInputTag);

    // send(path, header, message)로 영상 통화 신청 메시지를 전송한다. (모든 영상 통화 신청은 여기서 메시지를 전송한다.)
    // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
    // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
    stomp.send('/pub/chat/room/video', {}, JSON.stringify({type: "video", id: id, idx: idx, userIdx: userIdx}));
}

// 영상 통화 수락 메소드
function accept() {
    // webRtc.js에 있는 영상 통화 시작 메소드를 실행한다.
    startVideo();
    // send(path, header, message)로 영상 통화 신청 메시지를 전송한다. (모든 영상 통화 수락은 여기서 메시지를 전송한다.)
    // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
    // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
    stomp.send('/pub/chat/room/video', {}, JSON.stringify({type: "video", id: id, idx: idx, userIdx: userIdx, content: 'accept'}));
}

// 영상 통화 거절 메소드
function refuse() {
    // send(path, header, message)로 영상 통화 신청 메시지를 전송한다. (모든 영상 통화 거절은 여기서 메시지를 전송한다.)
    // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
    // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
    stomp.send('/pub/chat/room/video', {}, JSON.stringify({type: "video", id: id, idx: idx, userIdx: userIdx, content: 'refuse'}));
}
//////////////////////////////////////////////////// 채팅 구역 ////////////////////////////////////////////////////
// input 텍스트 태그에서 "keypress" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// keypress - 웹 페이지에서 키보드를 눌렀을 때 발생한다.
message.addEventListener("keypress", function(event) {
    // 눌린 키가 무엇인지 체크한다.
    // 눌린 키가 엔터 키인 경우
    if ( event.key === "Enter" ) {
        // 채팅 메시지 전송 메소드로 이동한다.
        send();
    }
});

// 채팅 메시지 전송 메소드
function send() {
    // 작성한 메시지를 가져와 값을 체크한다.
    // 가져온 메시지의 값이 비어있을 경우
    if ( message.value == "" ) {
        // 먼저 경고 알림창을 띄워준다.
        alert("메시지를 작성해주세요.");
        // 그 다음 아무 작업 없이 돌아간다.
        return;
    // 가져온 메시지의 값이 비어있지 않을 경우
    } else {
        // 1초 이내 5개 이상의 메시지를 보낼 경우 - 5초간 메시지를 보낼 수 없다.
        // ( end - start ) - 처음 메시지 전송후 1초 이내 다음 메시지 전송까지의 경과 시간
        if ( ( end - start ) <= 1000 && count >= 5 ) {
            alert("짧은 시간에 많은 메시지를 보낼 수 없습니다. 잠시 후 다시 시도해주세요.");
            // 먼저 로컬 스토리지에서 현재 가지고 있는 메시지 전송 상태를 제거한다.
            localStorage.removeItem("sendMessage");
            // 그 다음 메시지 전송 상태 값을 메시지를 보낼 수 없는 상태로 변경하여 다시 로컬 스토리지에 추가한다.
            localStorage.setItem("sendMessage", "false");
            // 마지막으로 로컬 스토리지에 다시 추가한 값을 가져와 알맞는 변수에 전달한다.
            sendMessage = localStorage.getItem("sendMessage");
            // 5초 대기 후 내부 메소드를 실행한다.
            setTimeout(function() {
                // 먼저 로컬 스토리지에서 현재 가지고 있는 시작 시간과 전송된 메시지 수와 메시지 전송 상태를 제거한다.
                localStorage.removeItem("start");
                localStorage.removeItem("count");
                localStorage.removeItem("sendMessage");
                // 그 다음 시작 시간과 전송된 메시지 수와 메시지 전송 상태 값을 다시 초기값으로 변경하여 로컬 스토리지에 추가한다.
                localStorage.setItem("start", Date.now());
                localStorage.setItem("count", 0);
                localStorage.setItem("sendMessage", "true");
                // 마지막으로 로컬 스토리지에 다시 추가한 값들을 가져와 각 알맞는 변수에 전달한다.
                start = localStorage.getItem("start");
                count = localStorage.getItem("count");
                sendMessage = localStorage.getItem("sendMessage");
            }, 5000);
        // 그 외 경우
        } else {
            // 메시지를 보낼 수 없는 상태인 경우 - 아직 5초 대기중이다.
            if ( sendMessage == "false" ) {
                // 받아온 메시지를 올리지 않고 통과
                return;
            // 메시지를 보낼 수 있는 상태인 경우
            } else {
                // 먼저 로컬 스토리지에서 현재 가지고 있는 메시지 전송 시간을 제거한다.
                localStorage.removeItem("end");
                // 그 다음 메시지 전송 시간 값을 다시 초기값으로 변경하여 로컬 스토리지에 추가한다.
                localStorage.setItem("end", Date.now());
                // 마지막으로 로컬 스토리지에 다시 추가한 값을 가져와 알맞는 변수에 전달한다.
                end = localStorage.getItem("end");
                // 메시지를 전송한지 1초가 지날 경우 - 다시 초기 상태로 돌아간다.
                if ( ( end - start ) > 1000 ) {
                    // 먼저 로컬 스토리지에서 현재 가지고 있는 시작 시간과 전송된 메시지 수를 제거한다.
                    localStorage.removeItem("start");
                    localStorage.removeItem("count");
                    // 그 다음 시작 시간과 전송된 메시지 수의 값을 다시 초기값으로 변경하여 로컬 스토리지에 추가한다.
                    localStorage.setItem("start", Date.now());
                    localStorage.setItem("count", 0);
                    // 마지막으로 로컬 스토리지에 다시 추가한 값들을 가져와 각 알맞는 변수에 전달한다.
                    start = localStorage.getItem("start");
                    count = localStorage.getItem("count");
                }
                // send(path, header, message)로 채팅 메시지를 전송한다. (입장 이후 작성되는 모든 메시지는 여기서 전송한다.)
                // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
                // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
                stomp.send('/pub/chat/room/message', {}, JSON.stringify({type: "chat", id: id, idx: idx, content: message.value}));
                message.value = ""; // 메시지를 전송한 뒤 공백 상태로 만든다.
                // 먼저 전송된 메시지 수를 1 증가시킨다.
                count++;
                // 그 다음 로컬 스토리지에서 현재 가지고 있는 전송된 메시지 수를 제거한다.
                localStorage.removeItem("count");
                // 그 다음 메시지 수의 값을 증가한 값으로 변경하여 다시 로컬 스토리지에 추가한다.
                localStorage.setItem("count", count);
                // 마지막으로 로컬 스토리지에 다시 추가한 값을 가져와 알맞는 변수에 전달한다.
                count = localStorage.getItem("count");
            }
        }
    }
}
////////////////////////////////////////////////// 녹음 변수 구역 //////////////////////////////////////////////////
let record = document.getElementById("record"); // 오디오 태그
let btnRecordStart = document.getElementById("btnRecordStart"); // 녹음 시작 버튼
let btnRecordStop = document.getElementById("btnRecordStop"); // 녹음 정지 버튼
let btnRecordSend = document.getElementById("btnRecordSend"); // 녹음 전송 버튼
let mediaRecorder; // MediaRecorder 객체
let recordedChunks = []; // blob 녹음 데이터
let recordTimeoutId; // 녹음 setTimeout ID
let recordBlob; // 병합한 Blob 녹음 데이터
let recordUrl; // 병합한 Blob 녹음 데이터의 URL
//////////////////////////////////////////////////// 녹음 구역 ////////////////////////////////////////////////////
// 녹음 시작 버튼 클릭 시 호출되는 메소드
async function startRecording() {
    // navigator.mediaDevices.getUserMedia() - 미디어 스트림(비디오 또는 오디오)을 생성하는 메소드로 비동기 메소드이며, 사용자의 미디어 장치(예: 마이크)를 사용할 수 있는 권한이 있는지를 확인하고, 권한이 있다면 미디어 스트림을 반한다.
    // getUserMedia() 메소드는 mediaDevices 객체에 호출되며, 사용자에게 미디어 장치 사용 권한을 요청한다.
    // getUserMedia()에 전달된 매개변수는 'audio' 속성을 true로 설정하여 사용자의 오디오 장치에 액세스하려는 것을 지정한다.
    // getUserMedia()는 Promise 객체를 반환하며,
    // 요청한 미디어 타입에 대한 스트림을 성공적으로 가져올 경우 then() 메소드를 호출하여 성공적인 MediaStream 객체를 반환한다.
    // 반면, 접근 권한이 거부되거나 미디어 장치가 없는 경우 catch() 메소드를 호출하여 NotAllowedError 또는 NotFoundError 오류를 반환한다.
    // getUserMedia()는 안전한 출처 (즉, localhost, HTTPS)에서만 작동한다.
    navigator.mediaDevices.getUserMedia({ audio: true })
        // then 메소드는 Promise 객체가 resolve된 후 호출된다.
        .then(stream => {
            try {
                // 브라우저가 WAV 오디오 형식을 지원하는지 검사한다.
                // WAV 오디오 형식을 지원하는 경우
                if ( typeof MediaRecorder.isTypeSupported == "function" && MediaRecorder.isTypeSupported( "audio/wav" ) ) {
                    // MediaRecorder 생성자를 사용하여 입력 스트림(stream)에서 오디오 데이터를 캡처하고,
                    // 해당 데이터의 MIME 타입을 "audio/wav"로 설정하는 MediaRecorder 객체를 생성해 mediaRecorder 변수에 전달한다.
                    // stream(입력 스트림) - getUserMedia() 메소드로 얻은 스트림을 저장하는 변수이며, 이를 MediaRecorder 생성자에 전달하여 오디오 데이터를 캡처한다.
                    mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/wav" });
                // WAV 오디오 형식을 지원하지 않는 경우
                } else {
                    // MediaRecorder 생성자를 사용하여 입력 스트림(stream)에서 오디오 데이터를 캡처하고,
                    // 해당 데이터의 MIME 타입을 "audio/webm"으로 설정하는 MediaRecorder 객체를 생성해 mediaRecorder 변수에 전달한다.
                    // stream(입력 스트림) - getUserMedia() 메소드로 얻은 스트림을 저장하는 변수이며, 이를 MediaRecorder 생성자에 전달하여 오디오 데이터를 캡처한다.
                    mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
                }

                // MediaRecorder 객체에서 "dataavailable" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                // dataavailable - MediaRecorder 객체에서 발생하는 이벤트 중 하나로 MediaRecorder가 녹음을 진행하면서 데이터를 누적할 때마다 발생하는 이벤트이다.
                //                 즉, 녹음된 오디오 데이터가 사용 가능해질 때마다 이 이벤트가 발생한다.
                //                 이 때 발생한 녹음된 오디오 데이터는 Blob 형태로 event.data에 담긴다.
                //                 그러기에 이 이벤트를 이용하면 MediaRecorder가 녹음한 오디오 데이터를 직접 다룰 수 있게 된다.
                mediaRecorder.addEventListener("dataavailable", function(event) {
                    // event.data에 들어있는 녹음된 오디오 데이터가 유효한지 체크한다.
                    // 녹음된 오디오 데이터가 유효하지 않은 경우
                    if (typeof event.data === "undefined") {
                        // 아무 작업 없이 돌아간다.
                        return;
                    // 녹음된 오디오 데이터가 유효한 경우
                    } else {
                        // 녹음된 오디오 데이터를 위에서 미리 만들어둔 recordedChunks 배열에 추가한다.
                        recordedChunks.push(event.data);
                        // Blob 생성자를 사용하여 recordedChunks 배열에 저장된 Blob 오디오 데이터들을 모두 가져와,
                        // MIME 타입을 "audio/webm"으로 설정하는 하나의 Blob 객체로 재생성해,
                        // recordBlob 변수에 전달한다.
                        recordBlob = new Blob(recordedChunks, { type: "audio/webm" });
                        // createObjectURL 메소드를 사용하여 재생성된 Blob 객체를 URL 객체로 변환해,
                        // recordUrl 변수에 전달한다.
                        recordUrl = URL.createObjectURL(recordBlob);
                        // .src를 사용하여 변환된 URL을 id값으로 가져온 현재 작성되어 있는 오디오 태그의 소스로 설정해,
                        // 녹음된 오디오를 전송하기 전에 녹음 내용을 들어 볼 수 있게 한다.
                        record.src = recordUrl;
                    }
                });

                // MediaRecorder의 start() 메소드를 호출하여 녹음을 시작한다.
                mediaRecorder.start();

                // setTimeout 메소드를 사용하여 녹음이 시작된지 5분이 경과하면 자동으로 녹음이 멈추고,
                // 이후 녹음을 더 이상 할 수 없도록 막고 초기화와 전송만 가능하도록 한다.
                // 이는 녹음 최대 시간이 5분을 넘지 않도록 하는 것이다.
                // 그리고 recordTimeoutId 변수에 setTimeout 메소드로 반환된 타이머 ID를 저장하여,
                // 타이머가 실행되는 동안 recordTimeoutId로 언제든지 타이머를 취소할 수 있도록 한다.
                recordTimeoutId = setTimeout(function() {
                    // MediaRecorder의 stop() 메소드를 호출하여 녹음을 멈춘다.
                    mediaRecorder.stop();
                    // 초기화 버튼을 활성화한다.
                    btnRecordStart.disabled = false;
                    // 재시작 버튼을 비활성화한다.
                    btnRecordStop.disabled = true;
                    // 전송 버튼을 활성화한다.
                    btnRecordSend.disabled = false;
                }, 300000);
            // catch 메소드는 Promise 객체가 reject된 후 호출된다.
            } catch (error) {
                // 오류가 발생한 경우, 해당 오류를 콘솔에 출력한다.
                console.error(error);
            }
    });
}

// 녹음된 오디오 메시지 전송 메소드
async function sendRecording() {
    // Promise 객체를 생성하여 async/await를 사용해 Blob 객체를 base64 문자열로 인코딩한다.
    // 해당 Promise 객체는 resolve를 호출하여 base64로 인코딩된 오디오 데이터를 반환해 base64Data 변수에 전달한다.
    const base64Data = await new Promise(resolve => {
        // FileReader 객체를 생성한다.
        // FileReader - 웹 API 중 하나로, 클라이언트 측 자바스크립트에서 파일의 내용을 비동기적으로 읽어들일 수 있도록 해주는 객체이다.
        //              이 객체를 이용하여 파일의 내용을 읽을 수 있으며, 이 때 파일의 종류나 형식에 따라 읽어들이는 방식이나 사용 방법이 다를 수 있다.
        const reader = new FileReader();
        // readAsDataURL() 메소드를 사용하여 Blob 객체를 base64 문자열로 변환한다.
        // readAsDataURL() - 메서드는 File 혹은 Blob 객체의 데이터를 읽어와, 해당 데이터를 Data URL 형태로 반환한다.
        //                   이 때 반환된 Data URL은 문자열이며, base64 인코딩된 형태의 데이터를 포함하고 있다.
        reader.readAsDataURL(recordBlob);
        // FileReader 객체에 onloadend 이벤트를 등록하여 파일 읽기 작업이 완료되면 호출될 콜백 메소드를 등록한다.
        reader.onloadend = function() {
            // Promise 객체에서 resolve 메소드를 호출하고,
            // reader.result를 사용하여 readAsDataURL()를 통해 변환한 base64 문자열 데이터를 가져와 반환한다.
            // resolve - Promise 객체에서 성공적으로 실행될 경우에 호출되며, 그 결과 값을 반환한다.
            //           여기서는 reader.result를 반환하므로, 이후 이 Promise 객체를 사용하는 메소드에서 reader.result 값을 받아 처리할 수 있다.
            resolve(reader.result);
        }
    });
    // send(path, header, message)를 사용하여 녹음된 오디오 데이터를 메시지로 전송한다. (모든 녹음된 오디오 데이터는 여기서 메시지를 전송한다.)
    // {id: id, idx: idx} - 전송할 메시지의 Header로, Header를 통해 id와 idx를 전송한다.
    // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 파라미터를 통해 받는다.
    stomp.send('/pub/chat/room/record', {id: id, idx: idx}, base64Data);
    // recordedChunks 배열을 초기화한다.
    recordedChunks = [];
    // 오디오 태그에 작성되있는 소스를 초기화한다.
    record.src = "";
    // 병합한 Blob 녹음 데이터의 URL을 초기화한다.
    recordUrl = "";
}

// 오디오 태그에서 "ended" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// ended - 오디오나 비디오 등의 미디어가 재생이 끝났을 때 발생하는 이벤트이다.
record.addEventListener("ended", function() {
    // 오디오 태그의 재생이 종료되면, 오디오 태그에 작성되어 있는 소스를 그대로 재작성한다.
    // 이는 오디오 재생이 끝나고 다시 재생할 때, 오디오 소스가 초기화되도록 하는 역할을 한다.
    // 이렇게 하는 이유는, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터를 온전히 다시 재생하기 위해서이다.
    // 이렇게 하지 않으면, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터중 마지막 오디오 데이터만 재생된다.
    // 하나의 오디오 데이터는 문제가 없지만, 병합된 여러개의 오디오 데이터는 이런 문제가 발생해 임시방편으로 찾아낸 방법이다.
    record.src = record.src;
});

// 시작 및 초기화 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordStart.addEventListener("click", function() {
    // 시작 버튼 이름을 체크한다.
    // "녹음 시작" 버튼을 클릭한 경우
    if ( btnRecordStart.value == "녹음 시작" ) {
        // 녹음 시작 메소드를 호출하여 녹음을 시작한다.
        startRecording();
        // 시작 버튼을 초기화 버튼으로 변경한다.
        btnRecordStart.value = "녹음 초기화";
        // 초기화 버튼을 비활성화한다.
        btnRecordStart.disabled = true;
        // 정지 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 전송 버튼을 비활성화한다.
        btnRecordSend.disabled = true;
    // "녹음 초기화" 버튼을 클릭한 경우
    } else {
        // 초기화 버튼을 시작 버튼으로 변경한다.
        btnRecordStart.value = "녹음 시작";
        // 시작 버튼을 활성화한다.
        btnRecordStart.disabled = false;
        // 재시작 버튼을 정지 버튼으로 변경한다.
        btnRecordStop.value = "녹음 정지";
        // 정지 버튼을 비활성화한다.
        btnRecordStop.disabled = true;
        // 전송 버튼을 비활성화한다.
        btnRecordSend.disabled = true;
        // recordedChunks 배열을 초기화한다.
        recordedChunks = [];
        // 현재 오디오 태그에 작성되있는 소스를 초기화한다.
        record.src = "";
    }
});

// 정지 및 재시작 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordStop.addEventListener("click", function() {
    // 정지 버튼 이름을 체크한다.
    // "녹음 정지" 버튼을 클릭한 경우
    if ( btnRecordStop.value == "녹음 정지" ) {
        // recordTimeoutId에 저장된 setTimeout ID를 사용하여 실행 중인 타이머를 취소한다.
        clearTimeout(recordTimeoutId);
        // MediaRecorder의 stop() 메소드를 호출하여 녹음을 멈춘다.
        mediaRecorder.stop();
        // 시작 버튼을 초기화 버튼으로 변경한다.
        btnRecordStart.value = "녹음 초기화";
        // 초기화 버튼을 활성화한다.
        btnRecordStart.disabled = false;
        // 정지 버튼을 재시작 버튼으로 변경한다.
        btnRecordStop.value = "녹음 재시작";
        // 재시작 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 전송 버튼을 활성화한다.
        btnRecordSend.disabled = false;
    // "녹음 재시작" 버튼을 클릭한 경우
    } else {
        // 오디오 태그에 작성되있는 소스를 초기화한다.
        record.src = "";
        // 병합한 Blob 녹음 데이터의 URL을 초기화한다.
        recordUrl = "";
        // 녹음 시작 메소드를 호출하여 녹음을 재시작한다.
        startRecording();
        // 초기화 버튼을 비활성화한다.
        btnRecordStart.disabled = true;
        // 재시작 버튼을 정지 버튼으로 변경한다.
        btnRecordStop.value = "녹음 정지";
        // 정지 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 전송 버튼을 비활성화한다.
        btnRecordSend.disabled = true;
    }
});

// 전송 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordSend.addEventListener("click", function() {
    // 녹음된 오디오 메시지 전송 메소드를 호출하여 녹음된 오디오를 메시지로 전송한다.
    sendRecording();
    // 초기화 버튼을 시작 버튼으로 변경한다.
    btnRecordStart.value = "녹음 시작";
    // 시작 버튼을 활성화한다.
    btnRecordStart.disabled = false;
    // 재시작 버튼을 정지 버튼으로 변경한다.
    btnRecordStop.value = "녹음 정지";
    // 정지 버튼을 비활성화한다.
    btnRecordStop.disabled = true;
    // 전송 버튼을 비활성화한다.
    btnRecordSend.disabled = true;
    // recordedChunks 배열을 초기화한다.
    recordedChunks = [];
});
///////////////////////////////////////////////// 페이지 이탈 구역 /////////////////////////////////////////////////
// 1. 페이지를 이탈하는 기능을 실행할 경우 발생하는 이벤트 핸들러 등록
// beforeunload - 탭 닫기, 윈도우 닫기, 페이지 닫기, 뒤로가기, 버튼, location.href, 새로고침 등 해당 페이지를 벗어나는 기능을 실행할 경우 항상 실행된다.
window.addEventListener("beforeunload", handleBeforeUnload);
// beforeunload 핸들러 메소드
function handleBeforeUnload(event) {
    // beforeunload 이벤트를 명시적으로 처리하지 않은 경우, 해당 이벤트에 기본 동작을 실행하지 않도록 지정한다.
    event.preventDefault();
    // beforeunload 경고창을 띄워준다. - 따로 메시지 작성을 안한 이유는 각 브라우저마다 기본으로 잡혀있는 메시지가 표시되기 때문이다.
    event.returnValue = "";
}

// 2. beforeunload 이벤트에서 반환한 경고창에 따라 <body>태그에 작성한 unload 이벤트에서 확인 및 취소를 체크하고, 확인을 누른 경우에만 지정한 메소드로 이동시킨다.
// unload - 어떤 방식으로든 페이지를 이탈하면 항상 실행된다.

// 3. 페이지 이탈 후 퇴장 메시지 전송 메소드 - unload에서 지정한 메소드
function exit() {
    // send(path, header, message)로 퇴장 메시지를 전송한다. (퇴장할때 딱 한번만 전송한다.)
    // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
    // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
    stomp.send('/pub/chat/room/exit', {}, JSON.stringify({type: "exit", id: id, idx: idx, userIdx: userIdx}));
}