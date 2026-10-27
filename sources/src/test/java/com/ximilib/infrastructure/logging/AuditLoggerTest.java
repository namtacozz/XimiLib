package com.ximilib.infrastructure.logging;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    @Test
    void testAuditLogWrite() throws Exception {
        String testAction = "TEST_ACTION";
        String testDetail = "Testing audit entry insertion";
        AuditLogger.log("TEST_USER", testAction, testDetail);

        File logFile = new File("logs/audit.log");
        assertTrue(logFile.exists(), "File logs/audit.log phải tồn tại");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertFalse(lines.isEmpty(), "File log không được rỗng");
        assertTrue(lines.stream().anyMatch(l -> l.contains(testAction) && l.contains("TEST_USER")),
            "Log phải chứa dòng vừa ghi");
    }
}
