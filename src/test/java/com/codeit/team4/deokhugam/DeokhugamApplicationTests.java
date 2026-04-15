package com.codeit.team4.deokhugam;

import com.codeit.team4.deokhugam.config.TestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
class DeokhugamApplicationTests {

    @Test
    void contextLoads() {
    }

}

