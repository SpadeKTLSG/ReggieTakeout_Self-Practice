package com.tlsg.takeout.config;

import com.tlsg.takeout.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    //存储静态资源的映射路径变量
    static final String BACKEND_RESOURCE = System.getProperty("user.dir") + "/src/main/resources/static/backend/";
    static final String FRONT_RESOURCE = System.getProperty("user.dir") + "/src/main/resources/static/front/";

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行SpadeK自定义静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("file:" + BACKEND_RESOURCE);

        registry.addResourceHandler("/front/**").addResourceLocations("file:" + FRONT_RESOURCE);

    }


    /**
     * 扩展mvc框架的消息转换器
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展SK消息转换器...");

        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());

        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0, messageConverter); //0表示在第一个位置添加
    }
}