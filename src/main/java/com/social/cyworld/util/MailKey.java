package com.social.cyworld.util;

import java.util.Random;

public class MailKey {
	private boolean lowerCheck;
    private int size;

    public String getKey(int size, boolean lowerCheck) {
        this.size = size;
        this.lowerCheck = lowerCheck;
        return init();
    }

    private String init() {
        Random ran = new Random();
        StringBuffer sb = new StringBuffer();
        int num  = 0;
        do {
            num = ran.nextInt(75) + 48;
            // 숫자 랜덤 생성
            if ((num >= 48 && num <= 57) || (num >= 65 && num <= 90) || (num >= 97 && num <= 122)) {
            	// 숫자를 글자로 변경
                sb.append((char) num);
            } else {
                continue;
            }
        } while (sb.length() < size); // do ~ while문이라 size와 같은 크기가 될때 종료
        if (lowerCheck) {
            return sb.toString().toLowerCase();
        }
        return sb.toString();
    }
}