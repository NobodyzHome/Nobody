package com.springboot.web.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.ArrayList;
import java.util.List;

@Component
// 在任意的组件类中（只要容器能装配到），只要有Spring MVC相关类型的Bean，即可被容器添加到spring MVC的配置中
public class MvcConfig implements WebMvcConfigurer {

    // 如果配置中有HttpMessageConverters，那么这个HttpMessageConverters的@Bean会覆盖spring boot默认的HttpMessageConverter配置
//    @Bean
//    public HttpMessageConverters converters() {
//        List<HttpMessageConverter<?>> converterList = new ArrayList<>();
//        converterList.add(new StringHttpMessageConverter());
//        // 如果在创建HttpMessageConverters时，传入第一个参数为false，就代表不添加默认的HttpMessageConverter
//        HttpMessageConverters converters = new HttpMessageConverters(false, converterList);
//
//        return converters;
//    }

    // 如果配置中有HttpMessageConverter的@Bean，那么容器会把这个HttpMessageConverter添加到当前的已注册的HttpMessageConverter的列表中
    // 注意：如果配置中也有HttpMessageConverters的@Bean，那么就会覆盖了当前这个HttpMessageConverter的配置
//    @Bean
//    public HttpMessageConverter<?> converter() {
//        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
//        return mappingJackson2HttpMessageConverter;
//    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setCookieName("language");
        cookieLocaleResolver.setCookieHttpOnly(true);

        return cookieLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("locale");

        return localeChangeInterceptor;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2XmlHttpMessageConverter());
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        System.out.println("test");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        System.out.println("test");
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        System.out.println("test");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        System.out.println("test");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

}
