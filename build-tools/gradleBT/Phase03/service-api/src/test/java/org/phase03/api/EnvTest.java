package org.phase03.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EnvTest {

    @Test
    void testEnv() {
        String applicationName  = System.getenv("APPLICATION_NAME");
        String applicationUser = System.getenv("APPLICATION_USER");
        String feat = System.getenv("FEATURE_X");

        System.out.println("APPLICATION_NAME=" + applicationName);
        System.out.println("APPLICATION_USER=" + applicationUser);
        System.out.println("FEATURE_X=" + feat);

        // Basic sanity checks (adjust as you like)
        assertNotNull(applicationName,  "APPLICATION_NAME must be set");
        assertNotNull(applicationUser, "APPLICATION_USER must be set");
        assertNotNull(feat, "FEATURE_X must be set");
    }
}
