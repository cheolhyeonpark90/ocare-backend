package com.ocare.ocarebackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ocare.ocarebackend.web.health.dto.deserializer.SafeDateTimeDeserializer;
import com.ocare.ocarebackend.web.health.dto.deserializer.SafeStepsDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        System.out.println("[DEBUG] Creating Custom WebConfig ObjectMapper");
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(LocalDateTime.class, new SafeDateTimeDeserializer());
        customModule.addDeserializer(Integer.class, new SafeStepsDeserializer());
        objectMapper.registerModule(customModule);

        return objectMapper;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        System.out
                .println("[DEBUG] Registering Custom MappingJackson2HttpMessageConverter via extendMessageConverters");

        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converters.add(0, converter);
    }
}
