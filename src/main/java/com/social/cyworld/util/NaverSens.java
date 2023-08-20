package com.social.cyworld.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public class NaverSens {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void send_msg(String phoneNumber, String smsKey, String smsPhoneNumber, String naverAccessKey, String naverSecretKey, String naverSensKey) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            // 호스트 URL
            String hostNameUrl = "https://sens.apigw.ntruss.com";
            // 요청 URL
            String requestUrl = "/sms/v2/services/";
            // 요청 URL Type
            String requestUrlType = "/messages";
            // 개인 인증키
            String accessKey = naverAccessKey;
            // 2차 인증을 위해 서비스마다 할당되는 service secret
            String secretKey = naverSecretKey;
            // 프로젝트에 할당된 SMS 서비스 Id
            String serviceId = naverSensKey;
            // 요청 method
            String method = "POST";
            // 1970년 1월 1일 00:00:00 협정 세계시(UTC)부터의 경과 시간을 밀리초(Millisecond)로 나타낸다.
            String timestamp = Long.toString(System.currentTimeMillis());
            // /sms/v2/services/{serviceId}/messages
            requestUrl += serviceId + requestUrlType;
            // https://sens.apigw.ntruss.com/sms/v2/services/{serviceId}/messages
            String smsUrl = hostNameUrl + requestUrl;

            // JSON 을 활용한 body data 생성
            JSONObject bodyJson = new JSONObject();
            JSONObject toJson = new JSONObject();
            JSONArray toArr = new JSONArray();

            toJson.put("to", phoneNumber);
            toJson.put("subject", "Cyworld 휴대폰 인증");
            // 난수와 함께 전송
            toJson.put("content", "[Cyworld 휴대폰 인증] 인증번호\n[" + smsKey + "]를 입력해 주세요.");
            toArr.put(toJson);

            // SMS Type (sms | lms | mms)
            bodyJson.put("type", "sms");
            // 메시지 Type (sms | AD)
            bodyJson.put("contentType", "COMM");
            // 국가 번호
            bodyJson.put("countryCode", "82");
            // 발신번호는 사전 등록된 발신번호만 사용 가능하다.
            bodyJson.put("from", smsPhoneNumber);
            // 기본 메시지 제목
            bodyJson.put("subject", "Cyworld 휴대폰 인증");
            // 기본 메시지 내용
            bodyJson.put("content", "[Cyworld 휴대폰 인증] 인증번호\n[" + smsKey + "]를 입력해 주세요.");
            // 메시지 정보
            // messages 내에 subject, content를 정의하지 않으면 기본 subject, content로 지정된 값으로 발송된다.
            // messages 내에 subject, content가 기본 subject, content 보다 우선 순위가 높다.
            // MMS type이지만 첨부하려는 파일이 없으면 LMS로 발송된다.
            bodyJson.put("messages", toArr);

            HttpPost httpPost = new HttpPost(smsUrl);
            // 요청 Body Content Type을 application/json으로 지정 (POST)
            httpPost.addHeader("content-type", "application/json; charset=utf-8");
            // API Gateway 서버와 시간 차가 5분 이상 나는 경우 유효하지 않은 요청으로 간주
            httpPost.setHeader("x-ncp-apigw-timestamp", timestamp);
            // 포털 또는 Sub Account에서 발급받은 Access Key ID
            httpPost.setHeader("x-ncp-iam-access-key", naverAccessKey);
            // Body를 Access Key Id와 맵핑되는 SecretKey로 암호화한 서명
            httpPost.setHeader("x-ncp-apigw-signature-v2", makeSignature(requestUrl, timestamp, method, accessKey, secretKey));

            // JSON 문자열을 UTF-8로 인코딩하여 HttpEntity로 설정
            HttpEntity httpEntity = new StringEntity(bodyJson.toString(), ContentType.APPLICATION_JSON);
            // HttpEntity 설정 및 요청 보내기
            httpPost.setEntity(httpEntity);

            HttpResponse response = httpClient.execute(httpPost);

            int responseCode = response.getStatusLine().getStatusCode();
            BufferedReader br;
            if (responseCode == 202) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            }

            String inputLine;
            StringBuilder responseBody = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                responseBody.append(inputLine);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Body를 Access Key Id와 맵핑되는 SecretKey로 암호화한 서명
    public static String makeSignature(
            String url,
            String timestamp,
            String method,
            String accessKey,
            String secretKey
    ) throws NoSuchAlgorithmException, InvalidKeyException {

        String space = " "; // one space
        String newLine = "\n"; // new line

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        String encodeBase64String;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            // HMAC 암호화 알고리즘은 HmacSHA256 사용
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
        } catch (UnsupportedEncodingException e) {
            encodeBase64String = e.toString();
        }


        return encodeBase64String;
    }
}
