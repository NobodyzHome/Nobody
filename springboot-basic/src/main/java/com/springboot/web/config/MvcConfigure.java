package com.springboot.web.config;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

// 同时自定义WebMvcConfigurer和WebMvcConfigurationSupport可能会发生问题
//@Component
public class MvcConfigure extends WebMvcConfigurationSupport {

    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {

        RequestMappingHandlerMapping requestMappingHandlerMapping = super.requestMappingHandlerMapping();
        requestMappingHandlerMapping.setRemoveSemicolonContent(false);
        return requestMappingHandlerMapping;
    }
}
