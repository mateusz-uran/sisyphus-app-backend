package io.github.mateuszuran.sisyphus_app;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class AbstractIntegrationTest {

    private static final MongoDBContainer mongodb;

    static {
        mongodb = new MongoDBContainer(DockerImageName.parse("mongo:6-jammy"));
        mongodb.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
}
