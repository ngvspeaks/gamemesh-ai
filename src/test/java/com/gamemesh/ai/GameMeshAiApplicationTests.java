package com.gamemesh.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                "org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration," +
                "org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration",
        "gamemesh.ai.provider=fallback"
})
class GameMeshAiApplicationTests {

    @Test
    void contextLoads() {
        // Confirms Spring ApplicationContext bootstrapping
    }
}

