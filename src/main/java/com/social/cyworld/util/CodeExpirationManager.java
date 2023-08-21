package com.social.cyworld.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CodeExpirationManager {
    private Map<String, String> codeMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void putCodeWithExpiration(String code, String redirectUri) {
        long expirationTimeMillis = 5 * 60 * 1000; // 5분을 밀리초로 변환
        codeMap.put(code, redirectUri);
        scheduler.schedule(new CodeExpirationTask(code), expirationTimeMillis, TimeUnit.MILLISECONDS);
    }

    public String getApiKeyIdx(String code) {
        String check = codeMap.get(code);
        codeMap.remove(code);
        return check;
    }

    private class CodeExpirationTask implements Runnable {
        private String code;

        public CodeExpirationTask(String code) {
            this.code = code;
        }

        @Override
        public void run() {
            codeMap.remove(code);
        }
    }
}