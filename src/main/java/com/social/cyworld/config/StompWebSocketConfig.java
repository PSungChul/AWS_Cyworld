package com.social.cyworld.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@EnableWebSocketMessageBroker // Stomp를 사용하기위해 선언하는 어노테이션
@Configuration
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //* endpoint 를 "/ws/meta" 로 설정하였는데 만약 해당 프로젝트에 스프링 시큐리티를 적용했다면 시큐리티에서 "/ws/meta/**" 경로는 통신을 허용하도록 해줘야 크로스도메인 관련 에러메시지가 발생하지 않는다.
    //
    //* setAllowOrigins() 설정도 역시 크로스도메인 이슈로 인해 필요한 설정이다.
    // 웹소켓은 연결할 때의 도메인주소와 요청할 때의 도메인주소가 일치해야 통신이 된다.
    // 만약 http://localhost:9999/ws/chat 으로 웹소켓 객체를 생성했는데(설정시점) http://localhost 로 웹소켓 통신을 시도하면(요청시점) connect가 이뤄지지 않는다.
    // 정확히 포트번호까지 일치해야 한다.
    // 따라서 설정된 도메인 외에 연결을 허용할 도메인을 지정해주면 해당 문제를 피할 수 있다.

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 채팅 소켓
        registry.addEndpoint("/ws/chat") // Client가 WebSocket 또는 SockJS에 connect할 경로
                .setAllowedOrigins("http://localhost:9999") // 도메인주소
                .withSockJS(); // SockJS
    }

    // 여기서는 송수신에 따라 각 접두사로 path를 지정하고, StompChatController 및 JavaScript에서 나머지 path를 지정할 수 있다.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // setApplicationDestinationPrefixes - Client에서 전송한 SEND 요청을 처리한다.
        registry.setApplicationDestinationPrefixes("/pub"); // 송신 - 전송 방향 : 클라이언트 --> 서버
        // enableSimpleBroker - 해당 경로로 SimpleBroker를 등록, SimpleBroker는 해당 경로를 SUBSCRIBE하는 Client에게 메세지를 전달하는 간단한 작업을 수행한다.
        registry.enableSimpleBroker("/sub") // 수신 - 전송 방향 : 서버 --> 클라이언트
                // 클라이언트와 서버간의 연결 상태를 유지하기 위해 주기적으로 보내는 heartbeat 메시지의 주기를 30초로 설정하고 있다.
                // setHeartbeatValue() - MQTT heartbeat를 설정하는 메소드이다.
                // MQTT heartbeat - 클라이언트와 서버간의 연결 상태를 유지하기 위해 주기적으로 보내는 메시지이다.
                .setHeartbeatValue(new long[]{30000, 30000})
                // heartBeatScheduler() 메소드에서 생성된 스케줄러를 사용하여, MQTT heartbeat를 주기적으로 보내는 작업을 스케줄링하고 있다.
                // setTaskScheduler() - MQTT heartbeat를 보내는 작업을 스케줄링하는 스케줄러를 설정하는 메소드이다. (@Bean으로 생성한 메소드를 가져와 설정한다.)
                // heartBeatScheduler() - MQTT heartbeat를 보내는 작업을 스케줄링하는 스케줄러를 생성하는 메소드이다. (@Bean으로 메소드를 생성한다.)
                .setTaskScheduler(heartBeatScheduler());
    }

    // MQTT heartbeat를 보내는 작업을 스케줄링하는 스케줄러를 생성한다.
    @Bean
    public TaskScheduler heartBeatScheduler() {
        // 이 메소드에서는 Spring Framework의 TaskScheduler 인터페이스를 구현한 ConcurrentTaskScheduler 빈을 반환한다.
        // ConcurrentTaskScheduler - 스프링 프레임워크에서 제공하는 TaskScheduler 인터페이스의 구현체로,
        //                           별도의 스레드를 생성하여 주기적으로 작업을 수행할 수 있는 스케줄러이다.
        return new ConcurrentTaskScheduler();
    }

    // WebSocket 전송 시, 메시지 크기 제한을 설정한다.
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 이 코드에서는 10MB (10 * 1024 * 1024 byte) 로 설정되어 있으므로, 10MB 이하의 메시지만 WebSocket으로 전송할 수 있다.
        registration.setMessageSizeLimit(10 * 1024 * 1024);
    }
}
