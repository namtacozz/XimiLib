package com.ximilib.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Kiểm tra các quy tắc phụ thuộc kiến trúc (Dependency Rules) của XimiLib bằng JUnit 5 thuần.
 * <p>
 * Các quy tắc kiến trúc theo AGENTS.md và ARCHITECTURE.md:
 * 1. domain không import: javax.swing, java.sql, com.zaxxer, com.ximilib.application,
 *    com.ximilib.infrastructure, com.ximilib.presentation, com.ximilib.gui.
 * 2. application không import: javax.swing, java.sql, com.ximilib.infrastructure,
 *    com.ximilib.presentation, com.ximilib.gui.
 * 3. presentation và gui không import: java.sql, com.ximilib.infrastructure.persistence.
 * 4. Không được tồn tại package com.ximilib.gui và com.ximilib.models (được ghi nhận trong baseline).
 * <p>
 * Cơ chế Baseline (arch-baseline.txt):
 * - Test CHỈ FAIL khi:
 *   (a) Xuất hiện vi phạm MỚI chưa được ghi nhận trong baseline.
 *   (b) Một vi phạm trong baseline đã được giải quyết nhưng chưa được xóa khỏi arch-baseline.txt (ép dọn sạch baseline).
 * - So khớp độc lập số dòng (dựa trên file + import) để tránh vỡ khi code dịch chuyển dòng.
 */
public class DependencyRuleTest {

    private static final String BASELINE_RESOURCE = "/arch-baseline.txt";

    // Danh sách package bị cấm theo từng tầng
    private static final List<String> DOMAIN_FORBIDDEN = List.of(
            "javax.swing",
            "java.sql",
            "com.zaxxer",
            "com.ximilib.application",
            "com.ximilib.infrastructure",
            "com.ximilib.presentation",
            "com.ximilib.gui"
    );

    private static final List<String> APPLICATION_FORBIDDEN = List.of(
            "javax.swing",
            "java.sql",
            "com.ximilib.infrastructure",
            "com.ximilib.presentation",
            "com.ximilib.gui"
    );

    private static final List<String> PRESENTATION_FORBIDDEN = List.of(
            "java.sql",
            "com.ximilib.infrastructure.persistence"
    );

    private static final Set<String> FORBIDDEN_PACKAGES = Set.of(
            "com.ximilib.gui",
            "com.ximilib.models"
    );

    /**
     * Đại diện cho một vi phạm import phát hiện được.
     */
    private static class ImportViolation {
        final Path filePath;
        final int lineNumber;
        final String importTarget;
        final String rule;
        final String key; // normalizePath(filePath) + " -> " + importTarget

        ImportViolation(Path filePath, int lineNumber, String importTarget, String rule) {
            this.filePath = filePath;
            this.lineNumber = lineNumber;
            this.importTarget = importTarget;
            this.rule = rule;
            this.key = normalizePath(filePath.toString()) + " -> " + importTarget;
        }
    }

    /**
     * Đại diện cho một mục import trong baseline file.
     */
    private static class BaselineImportEntry {
        final String rawLine;
        final int baselineLineNumber;
        final String normalizedPath;
        final String importTarget;
        final String key;

        BaselineImportEntry(String rawLine, int baselineLineNumber, String normalizedPath, String importTarget) {
            this.rawLine = rawLine;
            this.baselineLineNumber = baselineLineNumber;
            this.normalizedPath = normalizedPath;
            this.importTarget = importTarget;
            this.key = normalizedPath + " -> " + importTarget;
        }
    }

    @Test
    @DisplayName("Kiểm tra chiều phụ thuộc các tầng (Domain, Application, Presentation/GUI) đối chiếu Baseline")
    void testLayerDependencyRules() throws IOException {
        Path srcMainJava = resolveSourceDirectory();
        BaselineData baseline = loadBaseline();

        List<ImportViolation> detectedViolations = scanImportViolations(srcMainJava);

        Set<String> detectedKeys = detectedViolations.stream()
                .map(v -> v.key)
                .collect(Collectors.toSet());

        Set<String> baselineKeys = baseline.importEntries.keySet();

        List<String> newViolations = new ArrayList<>();
        for (ImportViolation v : detectedViolations) {
            if (!baselineKeys.contains(v.key)) {
                newViolations.add(String.format("  [+] %s:%d -> import %s; (Luật: %s)",
                        v.filePath.toString().replace('\\', '/'), v.lineNumber, v.importTarget, v.rule));
            }
        }

        List<String> staleBaselineEntries = new ArrayList<>();
        for (Map.Entry<String, BaselineImportEntry> entry : baseline.importEntries.entrySet()) {
            if (!detectedKeys.contains(entry.getKey())) {
                BaselineImportEntry b = entry.getValue();
                staleBaselineEntries.add(String.format("  [-] Dòng %d trong baseline: %s",
                        b.baselineLineNumber, b.rawLine));
            }
        }

        if (!newViolations.isEmpty() || !staleBaselineEntries.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n================================================================================\n");
            sb.append("LỖI KIỂM SOÁT KIẾN TRÚC TẦNG (LAYER DEPENDENCY RULES):\n");

            if (!newViolations.isEmpty()) {
                sb.append("\n(A) Phát hiện ").append(newViolations.size())
                        .append(" VI PHẠM MỚI KHÔNG CÓ TRONG BASELINE:\n");
                for (String err : newViolations) {
                    sb.append(err).append("\n");
                }
                sb.append("\n-> Hướng giải quyết: Tuân thủ Clean Architecture, đảo ngược phụ thuộc (DIP) hoặc chuyển logic sang tầng phù hợp.\n");
            }

            if (!staleBaselineEntries.isEmpty()) {
                sb.append("\n(B) Phát hiện ").append(staleBaselineEntries.size())
                        .append(" MỤC TRONG BASELINE ĐÃ ĐƯỢC GIẢI QUYẾT NHƯNG CHƯA XÓA:\n");
                for (String err : staleBaselineEntries) {
                    sb.append(err).append("\n");
                }
                sb.append("\n-> Hướng giải quyết: Hãy mở file sources/src/test/resources/arch-baseline.txt và xóa các dòng trên để hoàn tất dọn dẹp!\n");
            }
            sb.append("================================================================================\n");
            fail(sb.toString());
        }
    }

    @Test
    @DisplayName("Kiểm tra sự tồn tại của các package bị cấm (com.ximilib.gui, com.ximilib.models) đối chiếu Baseline")
    void testForbiddenPackages() throws IOException {
        Path srcMainJava = resolveSourceDirectory();
        BaselineData baseline = loadBaseline();

        Map<String, List<Path>> detectedPackageFiles = scanDeclaredPackages(srcMainJava);

        Set<String> detectedForbiddenPackages = new TreeSet<>();
        for (String pkg : detectedPackageFiles.keySet()) {
            for (String forbidden : FORBIDDEN_PACKAGES) {
                if (pkg.equals(forbidden) || pkg.startsWith(forbidden + ".")) {
                    detectedForbiddenPackages.add(forbidden);
                }
            }
        }

        List<String> newPackageViolations = new ArrayList<>();
        for (String forbidden : detectedForbiddenPackages) {
            if (!baseline.forbiddenPackages.contains(forbidden)) {
                newPackageViolations.add(String.format("  [+] Phát hiện package bị cấm mới: %s (chứa %d files)",
                        forbidden, detectedPackageFiles.getOrDefault(forbidden, Collections.emptyList()).size()));
            }
        }

        List<String> stalePackageEntries = new ArrayList<>();
        for (String baselinePkg : baseline.forbiddenPackages) {
            if (!detectedForbiddenPackages.contains(baselinePkg)) {
                stalePackageEntries.add(String.format("  [-] Package bị cấm trong baseline đã được dọn sạch hoàn toàn: %s",
                        baselinePkg));
            }
        }

        if (!newPackageViolations.isEmpty() || !stalePackageEntries.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n================================================================================\n");
            sb.append("LỖI KIỂM SOÁT PACKAGE BỊ CẤM (FORBIDDEN PACKAGE RULES):\n");

            if (!newPackageViolations.isEmpty()) {
                sb.append("\n(A) Phát hiện PACKAGE BỊ CẤM MỚI KHÔNG CÓ TRONG BASELINE:\n");
                for (String err : newPackageViolations) {
                    sb.append(err).append("\n");
                }
            }

            if (!stalePackageEntries.isEmpty()) {
                sb.append("\n(B) Phát hiện PACKAGE ĐÃ ĐƯỢC MIGRATION HOÀN TOÀN NHƯNG CHƯA XÓA KHỎI BASELINE:\n");
                for (String err : stalePackageEntries) {
                    sb.append(err).append("\n");
                }
                sb.append("\n-> Hướng giải quyết: Xóa dòng FORBIDDEN_PACKAGE:<package> tương ứng khỏi arch-baseline.txt!\n");
            }
            sb.append("================================================================================\n");
            fail(sb.toString());
        }
    }

    /**
     * Quét tất cả các file Java trong thư mục nguồn và phát hiện các vi phạm import.
     */
    private List<ImportViolation> scanImportViolations(Path srcMainJava) throws IOException {
        List<ImportViolation> violations = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(srcMainJava)) {
            List<Path> javaFiles = stream
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                    .sorted()
                    .toList();

            for (Path file : javaFiles) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                String packageName = extractPackageName(lines);
                if (packageName == null) {
                    continue;
                }

                List<String> forbiddenTargets = getForbiddenTargetsForPackage(packageName);
                if (forbiddenTargets.isEmpty()) {
                    continue;
                }

                for (int i = 0; i < lines.size(); i++) {
                    String rawLine = lines.get(i);
                    String importStatement = extractImportStatement(rawLine);
                    if (importStatement == null) {
                        continue;
                    }

                    for (String forbidden : forbiddenTargets) {
                        if (isViolatingImport(importStatement, forbidden)) {
                            String rule = describeRule(packageName, forbidden);
                            violations.add(new ImportViolation(file, i + 1, importStatement, rule));
                            break;
                        }
                    }
                }
            }
        }

        return violations;
    }

    /**
     * Xác định danh sách các package bị cấm dựa vào package của class hiện tại.
     */
    private List<String> getForbiddenTargetsForPackage(String packageName) {
        if (packageName.startsWith("com.ximilib.domain")) {
            return DOMAIN_FORBIDDEN;
        }
        if (packageName.startsWith("com.ximilib.application")) {
            return APPLICATION_FORBIDDEN;
        }
        if (packageName.startsWith("com.ximilib.presentation") || packageName.startsWith("com.ximilib.gui")) {
            return PRESENTATION_FORBIDDEN;
        }
        return Collections.emptyList();
    }

    private boolean isViolatingImport(String importStatement, String forbidden) {
        if (importStatement.equals(forbidden)) {
            return true;
        }
        if (importStatement.startsWith(forbidden + ".")) {
            return true;
        }
        if (importStatement.equals(forbidden + ".*")) {
            return true;
        }
        return false;
    }

    private String describeRule(String packageName, String forbidden) {
        if (packageName.startsWith("com.ximilib.domain")) {
            return "Domain không được import: " + forbidden;
        }
        if (packageName.startsWith("com.ximilib.application")) {
            return "Application không được import: " + forbidden;
        }
        if (packageName.startsWith("com.ximilib.presentation") || packageName.startsWith("com.ximilib.gui")) {
            return "Presentation/GUI không được import: " + forbidden;
        }
        return "Vi phạm luật phụ thuộc kiến trúc: " + forbidden;
    }

    /**
     * Quét các package khai báo của từng file.
     */
    private Map<String, List<Path>> scanDeclaredPackages(Path srcMainJava) throws IOException {
        Map<String, List<Path>> map = new LinkedHashMap<>();
        try (Stream<Path> stream = Files.walk(srcMainJava)) {
            List<Path> javaFiles = stream
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                    .toList();

            for (Path file : javaFiles) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                String pkg = extractPackageName(lines);
                if (pkg != null) {
                    map.computeIfAbsent(pkg, k -> new ArrayList<>()).add(file);
                }
            }
        }
        return map;
    }

    private String extractPackageName(List<String> lines) {
        for (String raw : lines) {
            String line = stripLineComments(raw).trim();
            if (line.startsWith("package ") && line.endsWith(";")) {
                return line.substring(8, line.length() - 1).trim();
            }
        }
        return null;
    }

    private String extractImportStatement(String rawLine) {
        String line = stripLineComments(rawLine).trim();
        if (line.startsWith("import ") && line.contains(";")) {
            int semi = line.indexOf(';');
            String stmt = line.substring(7, semi).trim();
            if (stmt.startsWith("static ")) {
                stmt = stmt.substring(7).trim();
            }
            return stmt;
        }
        return null;
    }

    private static String stripLineComments(String line) {
        int idx = line.indexOf("//");
        if (idx != -1) {
            return line.substring(0, idx);
        }
        return line;
    }

    /**
     * Chuẩn hóa đường dẫn file để so khớp độc lập hệ điều hành và thư mục gốc thực thi.
     */
    private static String normalizePath(String path) {
        String p = path.replace('\\', '/').trim();
        int idx = p.indexOf("com/ximilib/");
        if (idx != -1) {
            return p.substring(idx);
        }
        int srcIdx = p.indexOf("src/main/java/");
        if (srcIdx != -1) {
            return p.substring(srcIdx + "src/main/java/".length());
        }
        return p;
    }

    private static class BaselineData {
        final Map<String, BaselineImportEntry> importEntries = new LinkedHashMap<>();
        final Set<String> forbiddenPackages = new HashSet<>();
    }

    private BaselineData loadBaseline() throws IOException {
        BaselineData data = new BaselineData();
        List<String> lines = readBaselineLines();

        for (int i = 0; i < lines.size(); i++) {
            String rawLine = lines.get(i).trim();
            if (rawLine.isEmpty() || rawLine.startsWith("#")) {
                continue;
            }

            // Dạng luật package: FORBIDDEN_PACKAGE:<package> hoặc PACKAGE_RULE:<package>
            if (rawLine.startsWith("FORBIDDEN_PACKAGE:") || rawLine.startsWith("PACKAGE_RULE:") || rawLine.startsWith("PACKAGE:")) {
                int colon = rawLine.indexOf(':');
                String pkg = rawLine.substring(colon + 1).trim();
                data.forbiddenPackages.add(pkg);
                continue;
            }

            // Dạng import vi phạm: <đường-dẫn-file>:<số-dòng>:<import> hoặc <đường-dẫn-file>:<import>
            String[] parts = rawLine.split(":");
            if (parts.length >= 3) {
                String path = parts[0].trim();
                // parts[1] là số dòng
                String imp = parts[2].trim();
                String norm = normalizePath(path);
                BaselineImportEntry entry = new BaselineImportEntry(rawLine, i + 1, norm, imp);
                data.importEntries.put(entry.key, entry);
            } else if (parts.length == 2) {
                String path = parts[0].trim();
                String imp = parts[1].trim();
                String norm = normalizePath(path);
                BaselineImportEntry entry = new BaselineImportEntry(rawLine, i + 1, norm, imp);
                data.importEntries.put(entry.key, entry);
            }
        }

        return data;
    }

    private List<String> readBaselineLines() throws IOException {
        InputStream is = getClass().getResourceAsStream(BASELINE_RESOURCE);
        if (is != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().toList();
            }
        }

        // Fallback đọc trực tiếp từ hệ thống file nếu chạy ngoài classpath
        Path candidate1 = Paths.get("src/test/resources/arch-baseline.txt");
        if (Files.exists(candidate1)) {
            return Files.readAllLines(candidate1, StandardCharsets.UTF_8);
        }
        Path candidate2 = Paths.get("sources/src/test/resources/arch-baseline.txt");
        if (Files.exists(candidate2)) {
            return Files.readAllLines(candidate2, StandardCharsets.UTF_8);
        }

        throw new IOException("Không tìm thấy file arch-baseline.txt trên classpath lẫn hệ thống file!");
    }

    /**
     * Định vị thư mục nguồn Java chính xác dù chạy từ gốc repo hay từ thư mục sources.
     */
    private Path resolveSourceDirectory() {
        Path p1 = Paths.get("src/main/java");
        if (Files.exists(p1) && Files.isDirectory(p1)) {
            return p1;
        }
        Path p2 = Paths.get("sources/src/main/java");
        if (Files.exists(p2) && Files.isDirectory(p2)) {
            return p2;
        }
        throw new IllegalStateException("Không tìm thấy thư mục sources/src/main/java!");
    }
}
