package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:Demo;DB_CLOSE_ON_EXIT=FALSE")
class HelloControllerTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testHello() {
        assertThat(restTemplate.getForObject("/", String.class)).isEqualTo("Hello World!");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCalc() {
        Map<String, Object> body = restTemplate.getForObject("/calc?left=100&right=200", Map.class);
        assertThat(body).containsEntry("left", 100)
                .containsEntry("right", 200)
                .containsEntry("answer", 300);
    }
}
