package com.ximilib.infrastructure.jobs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BackgroundJobTest {

    @Test
    void testBackgroundJobLifecycle() {
        BackgroundJobManager manager = BackgroundJobManager.getInstance();
        assertNotNull(manager);
        assertDoesNotThrow(manager::start);
        assertDoesNotThrow(manager::stop);
    }
}
