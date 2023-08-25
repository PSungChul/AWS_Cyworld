package com.social.cyworld.httpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class NicePay {
    public static JsonNode getResponse(String payClientId, String payClientSecret, String tid, int amount) throws JsonProcessingException {
        // API URI
        String requestUrl = "https://sandbox-api.nicepay.co.kr/v1/payments/" + tid;

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((payClientId + ":" + payClientSecret).getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Body 설정
        String jsonBody = "{ \"amount\": " + amount + " }";

        //
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        // Rest API 객체
        RestTemplate restTemplate = new RestTemplate();

        // POST 방식 Request
        ResponseEntity<JsonNode> responseEntity = restTemplate.postForEntity(requestUrl, requestEntity, JsonNode.class);

        // Response 내용
        JsonNode responseNode = responseEntity.getBody();

        return responseNode;
    }

    public static JsonNode getCyworldUser(String accessToken) { // 19. 파라미터로 컨트롤러에서 넘어온 accessToken을 받아온다.
        // 20. 19에서 받아온 access_token을 가지고 구글 로그인 유저 정보를 받아오는 구글 서버 URL을 작성한다.
        final String requestUrl = "http://54.199.77.149:9999/api/user";

        // 21. 외부 서버와 통신을 맡아줄 HttpClient를 생성한다.
        final HttpClient client = HttpClientBuilder.create().build();
        // 22. 외부 서버와 통신할 때 사용할 HttpMethod를 생성하고, 파라미터에 20에서 작성한 URL을 전달한다.
        final HttpPost post = new HttpPost(requestUrl); // POST 방식

        // 23. 해당 메소드의 반환 값으로 사용할 변수를 미리 만들어둔다.
        JsonNode returnNode = null;

        // 외부 서버와 통신 시작
        try {
            // 24. 헤더에 Authorization으로 19에서 파라미터로 받아온 access_token을 추가한다.
            // addHeader - 헤더에 필요한 데이터들을 name/value 쌍으로 추가한다.
            post.addHeader("Authorization", "Bearer " + accessToken);
            // 25. 21에서 생성한 HttpClent에 22에서 생성한 HttpMethod를 전달하여 실행하고, HttpResponse로 반환되는 값을 받아온다.
            final HttpResponse response = client.execute(post);
            // 응답 상태 코드 얻기
//            final int responseCode = response.getStatusLine().getStatusCode();

//            System.out.println("\nSending 'GET' request to URL : " + requestUrl);
//            System.out.println("Response Code : " + responseCode);

            // 26. JSON 형식의 데이터를 파싱하기 위한 ObjectMapper를 생성한다.
            // ObjectMapper - JSON 형식의 데이터를 Java 객체로 역직렬화(Deserialize)하거나, 반대로 Java 객체를 JSON 형식의 데이터로 직렬화(Serialize)할 때 사용하는 Jackson 라이브러리의 클래스이다.
            ObjectMapper mapper = new ObjectMapper();
            // 27. 25에서 받아온 JSON 형식의 반환 값을 23에서 미리 만들어둔 변수에 해당 타입인 JsonNode 객체로 파싱하여 전달한다.
            returnNode = mapper.readTree(response.getEntity().getContent());

        } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
            e.printStackTrace();
        } catch (ClientProtocolException e) { // 클라이언트 프로토콜 예외
            e.printStackTrace();
        } catch (IOException e) { // I/O 예외
            e.printStackTrace();
        }

        // 28. 27에서 전달받은 값을 반환한다.
        return returnNode;
    }
}