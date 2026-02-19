package com.dvgs.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;

@EnabledIfSystemProperty(named = "it.db", matches = "true")
@SpringBootTest
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
