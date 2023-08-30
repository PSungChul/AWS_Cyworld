package com.social.cyworld.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private String connectPath = "/filePath/**"; // 리소스와 연결될 URL path를 지정한다. (클라이언트가 파일에 접근하기 위해 요청하는 url)
    // 실제 리소스가 존재하는 외부 경로를 지정한다.
    // 경로의 마지막은 반드시 " / "로 끝나야 하고, 로컬 디스크 경로인 경우 file:/// 접두어를 꼭 붙여야 한다.
    private String filePath = "file:///Users/p._.sc/IT/ToyProject/CyworldProject/util/files/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(connectPath) // 리소스와 연결될 URL path를 지정한다. (클라이언트가 파일에 접근하기 위해 요청하는 url)
                // 실제 리소스가 존재하는 외부 경로를 지정한다.
                // 경로의 마지막은 반드시 " / "로 끝나야 하고, 로컬 디스크 경로인 경우 file:/// 접두어를 꼭 붙여야 한다.
                .addResourceLocations(filePath);
    }
}
