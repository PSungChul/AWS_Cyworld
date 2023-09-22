let localVideo; // 로그인 유저 비디오
let remoteVideo; // 상대 유저 비디오
let hangupBtn; // 영상 통화 종료 버튼

// ICE 서버 설정을 정의한 객체
const configuration = {
    // Google의 STUN 서버를 사용하여 ICE 서버를 설정한다.
    iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
};

let localStream; // 로컬 미디어 스트림
let peerConnection; // WebRTC 피어 연결 (RTCPeerConnection 객체)
let socket; // WebSocket 연결을 담당하는 객체

// 'accept' 버튼을 클릭했을 때 WebSocket을 열어 Signaling Server와 연결 및 LocalStream 생성
async function startVideo() {
    // 영상 통화 버튼 구역을 비운다.
    videoBtnArea.innerHTML = '';

    // 종료 input 태그를 생성한다.
    let hangupInputTag = document.createElement('input');
    // 종료 input 태그의 class를 설정한다.
    hangupInputTag.setAttribute('class', 'videoBtn');
    // 종료 input 태그의 id를 설정한다.
    hangupInputTag.setAttribute('id', 'hangupBtn');
    // 종료 input 태그의 타입을 설정한다.
    hangupInputTag.type = 'button';
    // 종료 input 태그의 값을 설정한다.
    hangupInputTag.value = '통화 종료';
    // 종료 input 태그의 onclick을 설정한다.
    hangupInputTag.onclick = function() {
        hangup();
    };

    // 영상 통화 버튼 구역에 통화 input 태그를 올린다.
    videoBtnArea.appendChild(hangupInputTag);

    // 채팅 구역을 비운다.
    chatArea.innerHTML = '';

    // 상대 유저 video 태그를 생성한다.
    let youVideoTag = document.createElement('video');
    // 상대 유저 video 태그의 class를 설정한다.
    youVideoTag.setAttribute('class', 'chatVideo');
    // 상대 유저 video 태그의 id를 설정한다.
    youVideoTag.setAttribute('id', 'remoteVideo');
    // 상대 유저 video 태그의 playsinline을 설정한다.
    youVideoTag.setAttribute('playsinline', '');
    // 상대 유저 video 태그의 autoplay를 설정한다.
    youVideoTag.autoplay = true;

    // 상대 유저 div 태그를 생성한다.
    let youDivTag = document.createElement('div');
    // 상대 유저 div 태그의 class를 설정한다.
    youDivTag.setAttribute('class', 'video youVideo');
    // 상대 유저 div 태그에 상대 유저 video 태그를 올린다.
    youDivTag.appendChild(youVideoTag);

    // 로그인 유저 video 태그를 생성한다.
    let meVideoTag = document.createElement('video');
    // 로그인 유저 video 태그의 class를 설정한다.
    meVideoTag.setAttribute('class', 'chatVideo');
    // 로그인 유저 video 태그의 id를 설정한다.
    meVideoTag.setAttribute('id', 'localVideo');
    // 로그인 유저 video 태그의 playsinline을 설정한다.
    meVideoTag.setAttribute('playsinline', '');
    // 로그인 유저 video 태그의 autoplay를 설정한다.
    meVideoTag.autoplay = true;

    // 로그인 유저 div 태그를 생성한다.
    let meDivTag = document.createElement('div');
    // 로그인 유저 div 태그의 class를 설정한다.
    meDivTag.setAttribute('class', 'video meVideo');
    // 로그인 유저 div 태그에 로그인 유저 video 태그를 올린다.
    meDivTag.appendChild(meVideoTag);

    // 채팅 구역에 상대 유저 div 태그를 올린다.
    chatArea.appendChild(youDivTag);
    // 채팅 구역에 로그인 유저 div 태그를 올린다.
    chatArea.appendChild(meDivTag);

    // 로그인 유저와 상대 유저의 영상 통화 요소들을 각 알맞는 변수에 전달한다.
    localVideo = document.getElementById('localVideo'); // 로그인 유저 비디오
    remoteVideo = document.getElementById('remoteVideo'); // 상대 유저 비디오
    hangupBtn = document.getElementById('hangupBtn'); // 영상 통화 종료 버튼

    // WebSocket과 Signaling Server를 연결하는 메소드로 이동하여 WebSocket을 열어 Signaling Server와 연결 후 WebSocket 객체를 가져올 때까지 기다린다.
    socket = await openWebSocket();
    // 로컬 미디어 스트림이 존재하는지 체크한다.
    // 로컬 미디어 스트림이 존재하지 않는 경우
    if (!localStream) {
        try {
            // getUserMedia를 사용하여 로컬 미디어 스트림을 가져와 비디오 및 오디오를 사용한다.
            localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
            // 로그인 유저 비디오의 srcObject 속성에 가져온 로컬 미디어 스트림을 전달하여 로컬 카메라 영상을 출력한다.
            localVideo.srcObject = localStream;
        } catch (error) {
            console.error('Error getting media:', error);
            // 영상 통화 에러 알림창을 띄운다.
            alert('영상 통화에 에러가 발생하였습니다.\n채팅방에 다시 입장 후 재시도해주시기 바랍니다.');
            return;
        }
    }

    // WebRTC 연결 설정 메소드를 실행한다.
    setupWebRTC();
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// WebSocket과 Signaling Server 연결 메소드
function openWebSocket() {
    // Signaling Server의 URL 주소를 지정한다.
    const serverUrl = 'ws://localhost:1111';
    // Signaling Server의 URL 주소로 WebSocket 객체를 생성하여 Signaling Server와 연결한다.
    const ws = new WebSocket(serverUrl);

    // Signaling Server와 WebSocket의 연결이 열렸을 때 실행되는 이벤트 핸들러
    ws.addEventListener('open', () => {
        // 참여자 정보 메시지를 생성한다.
        const videoChat = {
            type: 'chat',
            id: id,
            idx: idx
        };

        // 참여자 정보 메시지를 Signaling Server에 전송한다.
        ws.send(JSON.stringify(videoChat));

        // Signaling Server와 WebSocket의 연결이 열린 이후 핑 메시지를 주기적으로 Signaling Server에 전송한다.
        setInterval(() => {
            // WebSocket 연결이 열려있는지 체크한다.
            // WebSocket 연결이 열려있는 경우
            if ( socket.readyState === socket.OPEN ) {
                // 핑 메시지를 Signaling Server에 전송한다.
                socket.send(JSON.stringify({ type: 'PING', message: 'PONG', id: id }));
            }
        }, 30000); // 30초마다 핑 메시지 전송
    });

    // WebSocket 객체를 반환한다.
    return ws;
}

// WebRTC 연결 설정 메소드
function setupWebRTC() {
    // ICE 서버 설정으로 RTCPeerConnection 객체를 생성하여 WebRTC 피어 연결을 설정한다.
    peerConnection = new RTCPeerConnection(configuration);

    // 로컬 미디어 스트림의 각 트랙을 모두 가져온다.
    localStream.getTracks().forEach(track => {
        // 가져온 트랙과 로컬 미디어 스트림을 설정한 WebRTC 피어 연결에 추가한다.
        peerConnection.addTrack(track, localStream);
    });

    // Signaling Server로부터 원격 미디어 스트림을 수신할 때 실행되는 이벤트 핸들러
    peerConnection.addEventListener('track', async (event) => {
        // 상대 유저 비디오의 srcObject 속성에 수신한 원격 미디어 스트림을 전달하여 원격 카메라 영상을 출력한다.
        remoteVideo.srcObject = event.streams[0];
    });

    // Offer SDP를 생성하는 메소드를 실행한다.
    createOffer().then(() => {
        // Offer SDP를 생성하고 전송까지 완료하면 메시지 처리 메소드를 실행한다.
        startMessageHandling();
    });
}

// Offer SDP 생성 메소드
async function createOffer() {
    try {
        // Offer SDP를 생성하고 완료될 때까지 기다린다.
        const offer = await peerConnection.createOffer();
        // 생성한 Offer SDP를 로컬 SDP로 설정하고 완료될 때까지 기다린다.
        await peerConnection.setLocalDescription(offer);
        // 로컬 SDP에 설정한 Offer SDP를 상대 유저에게 전송하는 메소드를 실행하고 완료될 때까지 기다린다.
        await sendOffer(peerConnection.localDescription);
    } catch (error) {
        console.error('Error creating and sending offer:', error);
        // 영상 통화 에러 알림창을 띄운다.
        alert('영상 통화에 에러가 발생하였습니다.\n채팅방에 다시 입장 후 재시도해주시기 바랍니다.');
        return;
    }
}

// Signaling Server로부터 메시지 수신 처리 메소드
function startMessageHandling() {
    // Signaling Server로부터 메시지를 수신할 때 실행되는 실행되는 이벤트 핸들러
    socket.addEventListener('message', (event) => {
        // Signaling Server로부터 수신한 메시지를 JavaScript 객체로 파싱한다.
        const message = JSON.parse(event.data);

        // 파싱한 메시지 타입을 체크한다.
        // Offer 타입인 경우
        if ( message.type === 'getOffer' ) {
            // Offer SDP 수신 메소드에 수신한 Offer SDP를 전달하여 실행한다.
            handleOffer(message.data.offer);
        // Answer 타입인 경우
        } else if ( message.type === 'getAnswer' ) {
            // Answer SDP 수신 메소드에 수신한 Answer SDP를 전달하여 실행한다.
            handleAnswer(message.data.answer);
        // ICE Candidate 타입인 경우
        } else if ( message.type === 'getCandidate' ) {
            // ICE Candidate 정보 수신 메소드에 수신한 ICE Candidate 정보를 전달하여 실행한다.
            handleCandidate(message.data.candidate);
        // 퇴장 타입인 경우
        } else if ( message.type === 'getExit' ) {
            // 영상 통화 종료 메소드를 실행한다.
            hangup();
        }
    });

    // WebSocket 연결이 닫힐 때 실행되는 이벤트 핸들러 - 통화 종료 버튼을 클릭한 경우 or 새로고침한 경우 or WebSocket 에러로 연결이 닫힌 경우 or Signaling Server 에러로 연결이 닫힌 경우
    socket.addEventListener('close', (event) => {
        // 퇴장 메시지를 생성한다.
        let exit = { type: 'exit', id: id, idx: idx };
        // 생성한 퇴장 메시지를 Signaling Server에 전송한다.
        socket.send(JSON.stringify(exit));
    });
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Offer SDP 전송 메소드
function sendOffer(offer) {
    // Offer 메시지를 생성한다.
    offer = { type: 'offer', offer: offer, id: id, idx: idx };
    // 생성한 Offer 메시지를 JSON 문자열로 변환하여 Signaling Server에 전송한다.
    socket.send(JSON.stringify(offer));

    // ICE Candidate 정보를 수집하여 상대 유저에게 전송한다.
    peerConnection.onicecandidate = (event) => {
        // 수집한 ICE Candidate 정보가 존재하는지 체크한다.
        // ICE Candidate 정보가 존재하는 경우
        if (event.candidate) {
            // 수집한 ICE Candidate 정보를 상대 유저에게 전송하는 메소드를 실행한다.
            sendCandidate(event.candidate);
        }
    };
}

// Offer SDP 수신 메소드
function handleOffer(offer) {
    // 수신한 Offer SDP를 원격 설명으로 설정한다.
    peerConnection.setRemoteDescription(new RTCSessionDescription(offer))
        // Answer SDP를 생성한다.
        .then(() => peerConnection.createAnswer())
        // 생성한 Answer SDP를 로컬 설명으로 설정한다.
        .then(answer => peerConnection.setLocalDescription(answer))
        .then(() => {
            // 로컬 설명에 설정한 Answer SDP를 상대 유저에게 전송하는 메소드를 실행한다.
            sendAnswer(peerConnection.localDescription);
        })
        .catch(error => {
            console.error('Error handling offer:', error);
            // 영상 통화 에러 알림창을 띄운다.
            alert('영상 통화에 에러가 발생하였습니다.\n채팅방에 다시 입장 후 재시도해주시기 바랍니다.');
            return;
        });
}

// Answer SDP 전송 메소드
function sendAnswer(answer) {
    // Answer 메시지를 생성한다.
    answer = { type: 'answer', answer: answer, id: id, idx: idx };
    // 생성한 Answer 메시지를 JSON 문자열로 변환하여 Signaling Server에 전송한다.
    socket.send(JSON.stringify(answer));

    // ICE Candidate 정보를 수집하여 상대 유저에게 전송한다.
    peerConnection.onicecandidate = (event) => {
        // 수집한 ICE Candidate 정보가 존재하는지 체크한다.
        // ICE Candidate 정보가 존재하는 경우
        if (event.candidate) {
            // 수집한 ICE Candidate 정보를 상대 유저에게 전송하는 메소드를 실행한다.
            sendCandidate(event.candidate);
        }
    };
}

// Answer SDP 수신 메소드
function handleAnswer(answer) {
    // 수신한 Answer SDP를 원격 설명으로 설정한다.
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer))
        .catch(error => {
            console.error('Error handling answer:', error);
            // 영상 통화 에러 알림창을 띄운다.
            alert('영상 통화에 에러가 발생하였습니다.\n채팅방에 다시 입장 후 재시도해주시기 바랍니다.');
            return;
        });
}

// ICE Candidate 정보 전송 메소드
function sendCandidate(candidate) {
    // ICE Candidate 정보 메시지를 생성한다.
    candidate = { type: 'candidate', candidate: candidate, id: id, idx: idx };
    // 생성한 ICE Candidate 정보 메시지를 JSON 문자열로 변환하여 Signaling Server에 전송한다.
    socket.send(JSON.stringify(candidate));
}

// ICE Candidate 정보 수신 메소드
function handleCandidate(candidate) {
    // 0.1초 대기 후 수신한 ICE Candidate 정보를 처리한다.
    setTimeout(() => {
        // 원격 설명이 존재하는지 체크한다.
        // 원격 설명이 존재하는 경우
        if (peerConnection.remoteDescription) {
                // 수신한 ICE Candidate 정보를 원격 피어에 추가한다.
                peerConnection.addIceCandidate(new RTCIceCandidate(candidate))
                    .catch(error => {
                        console.error('Error adding ICE candidate:', error);
                        // 영상 통화 에러 알림창을 띄운다.
                        alert('영상 통화에 에러가 발생하였습니다.\n채팅방에 다시 입장 후 재시도해주시기 바랍니다.');
                        return;
                    });
        }
    }, 100); // 0.1초 후에 실행
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 연결 종료 버튼을 클릭했을 때 호출되는 함수
function hangup() {
    // 피어 연결이 존재하는지 체크한다.
    // 피어 연결이 존재하는 경우
    if (peerConnection) {
        // 피어 연결을 닫는다.
        peerConnection.close();
    }

    // 로컬 미디어 스트림이 존재하는지 체크한다.
    // 로컬 미디어 스트림이 존재하는 경우
    if (localStream) {
        // 로컬 미디어 스트림의 각 트랙을 모두 가져와 중지한다.
        localStream.getTracks().forEach(track => track.stop());
    }

    // WebSocket 연결이 열려있는지 체크한다.
    // WebSocket 연결이 열려있는 경우
    if (socket) {
        // WebSocket 연결을 닫는다.
        socket.close();
    }

    // 영상 통화 종료 알림창을 띄운다.
    alert('영상 통화가 종료되었습니다.');
    // 페이지를 새로고침한다.
    window.location.reload();
}