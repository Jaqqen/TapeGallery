package com.jaqqen.tapeshub;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {
    @Test
    void verifyModularity() {
        ApplicationModules.of(TapesHubApplication.class).verify();
    }
}
