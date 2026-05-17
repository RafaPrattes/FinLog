package com.ucb.finlog.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AppConfigTest {
    @Test
    void deveCriarRestTemplate() {
        assertInstanceOf(RestTemplate.class, new AppConfig().restTemplate());
    }
}
