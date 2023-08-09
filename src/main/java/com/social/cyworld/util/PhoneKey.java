package com.social.cyworld.util;

import org.springframework.boot.configurationprocessor.json.JSONException;

import java.util.Random;

public class PhoneKey {
    public static String randomSmsKey(String phoneNumber, String smsPhoneNumber, String naverAccessKey, String naverSecretKey, String naverSensKey) throws JSONException {
        NaverSens message = new NaverSens();
        Random rand = new Random();
        String numStr = "";
        for (int i = 0; i < 6; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr += ran;
        }

        // 생성된 인증번호 전송
        message.send_msg(phoneNumber, numStr, smsPhoneNumber, naverAccessKey, naverSecretKey, naverSensKey);

        return numStr;
    }
}
