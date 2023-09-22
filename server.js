// Express 웹 프레임워크 모듈을 불러온다.
const express = require('express');
// HTTP 모듈을 불러온다.
const http = require('http');
// WebSocket 모듈을 불러온다.
const WebSocket = require('ws');

// Express 애플리케이션을 생성한다.
const app = express();
// HTTP 서버를 생성하고 Express 애플리케이션을 서버에 연결한다.
const server = http.createServer(app);
// WebSocket 서버를 생성하고 HTTP 서버에 연결한다.
const wss = new WebSocket.Server({ server });

// 기본 주소로 GET 요청을 처리한다. - 대상 그룹 상태 확인용
app.get('/', (req, res) => {
    res.send('Signaling Server');
});

// 채팅방 정보들
let chatRooms = {};
// 채팅방 최대 인원 수
const maximum = 2;

// 클라이언트와 WebSocket의 연결이 열렸을 때 실행되는 이벤트 핸들러
wss.on('connection', (ws) => {
    // 클라이언트의 IP 주소를 가져온다.
    const clientAddress = ws._socket.remoteAddress;
    // 클라이언트의 포트 번호를 가져온다.
    const clientPort = ws._socket.remotePort;

    // 클라이언트로부터 메시지를 수신할 때 실행되는 실행되는 이벤트 핸들러
    ws.on('message', (data) => {
        // 클라이언트로부터 수신한 메시지를 JavaScript 객체로 파싱한다.
        const message = JSON.parse(data);
        
        // 파싱한 메시지의 타입을 체크한다.
        // 클라이언트와 WebSocket의 연결이 열린 이후 처음으로 전송한 참여자 정보 메시지 타입인 경우
        if (message.type === 'chat') {
            // 채팅방 아이디에 해당하는 채팅방 정보가 존재하는지 체크한다.
            // 채팅방 정보가 존재하는 경우
            if (chatRooms[message.id]) {
                // 채팅방 아이디에 해당하는 채팅방에 참여중인 인원을 체크하여 참여중인 인원과 최대 인원을 비교한다. - 1:1 상황에서는 필요하지 않지만, 1:N 상황에서는 필요하다.
                const length = chatRooms[message.id].length;

                // 참여중인 인원이 최대 인원 수와 같은 경우
                if (length === maximum) {
                    // Full 메시지를 생성한다.
                    const full = { type: 'full' };
                    // 생성한 Full 메시지를 JSON 문자열로 변환하여 클라이언트에 전송한다.
                    ws.send(JSON.stringify(full));
                    //
                    return;
                }

                // 참여중인 인원이 최대 인원보다 적은 경우
                
                // 참여자 정보를 가져와 채팅방 아이디에 해당하는 채팅방 정보에 추가한다.
                chatRooms[message.id].push({ idx: message.idx, ip: clientAddress, port: clientPort });
            // 채팅방 정보가 존재하지 않는 경우
            } else {
                // 참여자 정보를 가져와 채팅방 아이디에 해당하는 채팅방 정보를 생성하며 추가한다.
                chatRooms[message.id] = [{ idx: message.idx, ip: clientAddress, port: clientPort }];
            }
        // Offer 타입인 경우
        } else if (message.type === 'offer') {
            // 채팅방 아이디에 해당하는 채팅방 정보를 가져온다.
            const chatUsers = chatRooms[message.id];

            // 가져온 채팅방 정보가 존재하는지 체크한다.
            // 채팅방 정보가 존재하는 경우
            if (chatUsers) {
                // 메시지 전송자의 IP 주소와 포트 번호를 가져와 메시지 전송자 정보에 추가한다.
                const sender = { ip: clientAddress, port: clientPort };

                // 가져온 채팅방 정보에서 각 참여자 정보를 모두 가져온다.
                chatUsers.forEach((user) => {
                    // WebSocket과의 연결이 열려있는 각 클라이언트 정보를 모두 가져온다.
                    wss.clients.forEach((client) => {
                        // 가져온 클라이언트 정보와 가져온 참여자 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                        // IP 주소와 포트 번호가 일치하는 경우 - 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                        if (client._socket.remoteAddress === user.ip && client._socket.remotePort === user.port) {
                            // 가져온 클라이언트 정보와 메시지 전송자의 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                            // IP 주소와 포트 번호가 일치하지 않는 경우 - 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                            if (client._socket.remoteAddress !== sender.ip || client._socket.remotePort !== sender.port) {
                                // 수신한 Offer 메시지를 JSON 문자열로 변환하여 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 각 클라이언트에 전송한다.
                                client.send(JSON.stringify({ type: 'getOffer', data: message }));
                            }
                        }
                    });
                });
            }
        // Answer 타입인 경우
        } else if (message.type === 'answer') {
            // 채팅방 아이디에 해당하는 채팅방 정보를 가져온다.
            const chatUsers = chatRooms[message.id];

            // 가져온 채팅방 정보가 존재하는지 체크한다.
            // 채팅방 정보가 존재하는 경우
            if (chatUsers) {
                // 메시지 전송자의 IP 주소와 포트 번호를 가져온다.
                const sender = { ip: clientAddress, port: clientPort };
                
                // 가져온 채팅방 정보에서 각 참여자 정보를 모두 가져온다.
                chatUsers.forEach((user) => {
                    // WebSocket과의 연결이 열려있는 각 클라이언트 정보를 모두 가져온다.
                    wss.clients.forEach((client) => {
                        // 가져온 클라이언트 정보와 가져온 참여자 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                        // IP 주소와 포트 번호가 일치하는 경우 - 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                        if (client._socket.remoteAddress === user.ip && client._socket.remotePort === user.port) {
                            // 가져온 클라이언트 정보와 메시지 전송자의 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                            // IP 주소와 포트 번호가 일치하지 않는 경우 - 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                            if (client._socket.remoteAddress !== sender.ip || client._socket.remotePort !== sender.port) {
                                // 수신한 Answer 메시지를 JSON 문자열로 변환하여 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 각 클라이언트에 전송한다.
                                client.send(JSON.stringify({ type: 'getAnswer', data: message }));
                            }
                        }
                    });
                });
            }
        // ICE Candidate 타입인 경우
        } else if (message.type === 'candidate') {
            // 채팅방 아이디에 해당하는 채팅방 정보를 가져온다.
            const chatUsers = chatRooms[message.id];

            // 가져온 채팅방 정보가 존재하는지 체크한다.
            // 채팅방 정보가 존재하는 경우
            if (chatUsers) {
                // 메시지 전송자의 IP 주소와 포트 번호를 가져온다.
                const sender = { ip: clientAddress, port: clientPort };

                // 가져온 채팅방 정보에서 각 참여자 정보를 모두 가져온다.
                chatUsers.forEach((user) => {
                    // WebSocket과의 연결이 열려있는 각 클라이언트 정보를 모두 가져온다.
                    wss.clients.forEach((client) => {
                        // 가져온 클라이언트 정보와 가져온 참여자 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                        // IP 주소와 포트 번호가 일치하는 경우 - 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                        if (client._socket.remoteAddress === user.ip && client._socket.remotePort === user.port) {
                            // 가져온 클라이언트 정보와 메시지 전송자의 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                            // IP 주소와 포트 번호가 일치하지 않는 경우 - 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                            if (client._socket.remoteAddress !== sender.ip || client._socket.remotePort !== sender.port) {
                                // 수신한 ICE Candidate 정보 메시지를 JSON 문자열로 변환하여 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 각 클라이언트에 전송한다.
                                client.send(JSON.stringify({ type: 'getCandidate', data: message }));
                            }
                        }
                    });
                });
            }
        // 클라이언트와 WebSocket의 연결이 닫힌 이후 마지막으로 전송한 퇴장 메시지 타입인 경우
        } else if (message.type === 'exit') {
            // 채팅방 아이디에 해당하는 채팅방 정보를 가져온다.
            const chatUsers = chatRooms[message.id];

            // 가져온 채팅방 정보가 존재하는지 체크한다.
            // 채팅방 정보가 존재하는 경우
            if (chatUsers) {
                // 메시지 전송자의 IP 주소와 포트 번호를 가져온다.
                const sender = { ip: clientAddress, port: clientPort };

                // 가져온 채팅방 정보에서 각 참여자 정보를 모두 가져온다.
                chatUsers.forEach((user) => {
                    // WebSocket과의 연결이 열려있는 각 클라이언트 정보를 모두 가져온다.
                    wss.clients.forEach((client) => {
                        // 가져온 클라이언트 정보와 가져온 참여자 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                        // IP 주소와 포트 번호가 일치하는 경우 - 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                        if (client._socket.remoteAddress === user.ip && client._socket.remotePort === user.port) {
                            // 가져온 클라이언트 정보와 메시지 전송자의 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
                            // IP 주소와 포트 번호가 일치하지 않는 경우 - 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 클라이언트
                            if (client._socket.remoteAddress !== sender.ip || client._socket.remotePort !== sender.port) {
                                // 수신한 퇴장 메시지를 JSON 문자열로 변환하여 메시지 전송자를 제외한 채팅방 아이디에 해당하는 채팅방에 참여중인 각 클라이언트에 전송한다.
                                client.send(JSON.stringify({ type: 'getExit', data: message }));
                            }
                        }
                    });
                });

                // 채팅방 아이디에 해당하는 채팅방 정보를 삭제한다. - 1:1 상황에서는 채팅방 정보를 바로 삭제하지만, 1:N 상황에서는 채팅방 아이디에 해당하는 채팅방에서 퇴장자만 삭제하고 이후 마지막 한 명까지 나가면 그때 채팅방 정보를 삭제한다.
                delete chatRooms[message.id];
            }
        }
    });

    // WebSocket 연결이 닫힐 때 실행되는 이벤트 핸들러 - WebSocket과의 연결이 열려있는 클라이언트가 단 한명도 없는 경우 or WebSocket 에러로 연결이 닫힌 경우
    ws.on('close', () => {
        // 마지막 퇴장자의 IP 주소와 포트 번호를 가져온다.
        const sender = { ip: clientAddress, port: clientPort };

        // WebSocket과의 연결이 열려있는 각 클라이언트 정보를 모두 가져온다.
        wss.clients.forEach((client) => {
            // 가져온 클라이언트 정보와 마지막 퇴장자의 정보에서 IP 주소와 포트 번호를 가져와 일치하는지 체크한다.
            // IP 주소와 포트 번호가 일치하지 않는 경우 - 마지막 퇴장자를 제외한 WebSocket과의 연결이 열려있는 클라이언트
            if (client._socket.remoteAddress !== sender.ip || client._socket.remotePort !== sender.port) {
                // 퇴장 메시지를 생성한다.
                let exit = { type: 'getExit' };
                // 생성한 퇴장 메시지를 JSON 문자열로 변환하여 마지막 퇴장자를 제외한 WebSocket과의 연결이 열려있는 각 클라이언트에 전송한다.
                client.send(JSON.stringify(exit));
            }
        });

        // 채팅방 정보들을 모두 삭제하여 처음 상태로 초기화한다.
        chatRooms = {};
    });
});

// Signaling Server를 1111 포트에서 실행한다.
server.listen(1111);