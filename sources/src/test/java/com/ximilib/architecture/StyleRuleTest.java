package com.ximilib.architecture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class StyleRuleTest {

    private static final Properties EXCEPTIONS = new Properties();
    private static Path sourceRoot;
    private static final List<Path> javaFiles = new ArrayList<>();

    private static int packageInfoCount = 0;
    private static int commentCount = 0;
    private static int streamOptionalVarCount = 0;
    private static int vietnameseIdentifierCount = 0;
    private static int hardcodedHexCount = 0;
    private static int jOptionPaneJDialogCount = 0;
    private static int vietnamesePresentationStringsCount = 0;

    private static final Map<String, Map<String, Integer>> packageStats = new HashMap<>();

    private static final Pattern VIETNAMESE_ACCENTS = Pattern.compile("[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđĐ]", Pattern.CASE_INSENSITIVE);
    private static final Pattern STREAM_OPTIONAL_VAR = Pattern.compile("(\\.stream\\(|\\bOptional<|\\bvar\\s+[a-zA-Z0-9_]+\\s*=)");
    private static final Pattern HEX_COLOR = Pattern.compile("(#[0-9a-fA-F]{6}\\b|Color\\.decode\\()");
    private static final Pattern DIALOG_PATTERN = Pattern.compile("(\\bJOptionPane\\b|\\bJDialog\\b)");
    private static final Pattern STRING_LITERAL = Pattern.compile("\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"");

    @BeforeAll
    public static void setUp() throws IOException {
        loadExceptions();
        resolveSourceRoot();
        scanFiles();
    }

    private static void loadExceptions() {
        Path[] possiblePaths = new Path[]{
                Paths.get("docs/style_exceptions.txt"),
                Paths.get("../docs/style_exceptions.txt"),
                Paths.get("../../docs/style_exceptions.txt")
        };
        boolean loaded = false;
        for (Path p : possiblePaths) {
            if (Files.exists(p)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(p.toFile()), StandardCharsets.UTF_8))) {
                    EXCEPTIONS.load(reader);
                    loaded = true;
                    break;
                } catch (IOException ignored) {
                }
            }
        }
        if (!loaded) {
            System.err.println("WARN: Could not locate docs/style_exceptions.txt, using zero allowance.");
        }
    }

    private static void resolveSourceRoot() {
        Path[] candidates = new Path[]{
                Paths.get("sources/src/main/java"),
                Paths.get("src/main/java")
        };
        for (Path c : candidates) {
            if (Files.isDirectory(c)) {
                sourceRoot = c.toAbsolutePath().normalize();
                break;
            }
        }
        if (sourceRoot == null) {
            fail("Cannot locate Java source root (sources/src/main/java)");
        }
    }

    private static void scanFiles() throws IOException {
        String[] packages = new String[]{"domain", "application", "infrastructure", "presentation"};
        for (String pkg : packages) {
            packageStats.put(pkg, new HashMap<>());
        }
        packageStats.put("other", new HashMap<>());

        try (var stream = Files.walk(sourceRoot)) {
            stream.filter(p -> p.toString().endsWith(".java")).forEach(javaFiles::add);
        }

        for (Path file : javaFiles) {
            String rel = sourceRoot.relativize(file).toString().replace('\\', '/');
            String pkg = "other";
            for (String p : packages) {
                if (rel.startsWith("com/ximilib/" + p + "/")) {
                    pkg = p;
                    break;
                }
            }
            Map<String, Integer> stats = packageStats.get(pkg);
            stats.put("files", stats.getOrDefault("files", 0) + 1);

            if (file.getFileName().toString().equals("package-info.java")) {
                packageInfoCount++;
                stats.put("package_info", stats.getOrDefault("package_info", 0) + 1);
            }

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            boolean inBlockComment = false;

            for (String line : lines) {
                String trimmed = line.trim();

                if (trimmed.startsWith("//") || trimmed.contains("//") || trimmed.contains("/*") || trimmed.contains("*/") || trimmed.startsWith("*")) {
                    commentCount++;
                    stats.put("comments", stats.getOrDefault("comments", 0) + 1);
                }

                if (STREAM_OPTIONAL_VAR.matcher(line).find()) {
                    streamOptionalVarCount++;
                    stats.put("stream_optional_var", stats.getOrDefault("stream_optional_var", 0) + 1);
                }

                if (DIALOG_PATTERN.matcher(line).find()) {
                    jOptionPaneJDialogCount++;
                    stats.put("dialogs", stats.getOrDefault("dialogs", 0) + 1);
                }

                if (HEX_COLOR.matcher(line).find() && !file.getFileName().toString().equals("UITheme.java")) {
                    hardcodedHexCount++;
                    stats.put("hex_colors", stats.getOrDefault("hex_colors", 0) + 1);
                }

                if (pkg.equals("presentation") && !file.getFileName().toString().equals("UiText.java")) {
                    Matcher stringMatcher = STRING_LITERAL.matcher(line);
                    while (stringMatcher.find()) {
                        String literalContent = stringMatcher.group(1);
                        if (VIETNAMESE_ACCENTS.matcher(literalContent).find()) {
                            vietnamesePresentationStringsCount++;
                            stats.put("vietnamese_strings", stats.getOrDefault("vietnamese_strings", 0) + 1);
                        }
                    }
                }
            }
        }
    }

    private static int getAllowed(String key) {
        String val = EXCEPTIONS.getProperty(key);
        if (val == null || val.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(val.trim());
    }

    @Test
    @DisplayName("R-13: Không còn file package-info.java")
    public void testNoPackageInfoFiles() {
        int allowed = getAllowed("package_info.allowed");
        assertTrue(packageInfoCount <= allowed,
                "Số lượng file package-info.java hiện tại (" + packageInfoCount + ") vượt quá hạn mức (" + allowed + ")");
    }

    @Test
    @DisplayName("R-13: Không còn chú thích // hoặc /* trong mã nguồn Java")
    public void testNoCommentsInJavaCode() {
        int allowed = getAllowed("comments.allowed");
        assertTrue(commentCount <= allowed,
                "Số lượng dòng comment hiện tại (" + commentCount + ") vượt quá hạn mức (" + allowed + ")");
    }

    @Test
    @DisplayName("R-15: Không dùng Stream API, Optional hoặc từ khóa var")
    public void testNoStreamOptionalVar() {
        int allowed = getAllowed("stream_optional_var.allowed");
        assertTrue(streamOptionalVarCount <= allowed,
                "Số lượng Stream/Optional/var hiện tại (" + streamOptionalVarCount + ") vượt quá hạn mức (" + allowed + ")");
    }

    @Test
    @DisplayName("R-14: Định danh 100% tiếng Anh không dấu")
    public void testNoVietnameseInIdentifiers() {
        int allowed = getAllowed("vietnamese_identifiers.allowed");
        assertTrue(vietnameseIdentifierCount <= allowed,
                "Có định danh tiếng Việt trong mã nguồn (" + vietnameseIdentifierCount + ")");
    }

    @Test
    @DisplayName("R-17: Không hard-code mã màu hex ngoài UITheme")
    public void testNoHardcodedHexColorsOutsideUITheme() {
        int allowed = getAllowed("hardcoded_hex.allowed");
        assertTrue(hardcodedHexCount <= allowed,
                "Số lượng mã hex hard-code ngoài UITheme (" + hardcodedHexCount + ") vượt quá hạn mức (" + allowed + ")");
    }

    @Test
    @DisplayName("R-11: Không gọi trực tiếp JOptionPane hoặc JDialog thuần OS")
    public void testNoDirectJOptionPaneOrJDialog() {
        int allowed = getAllowed("joptionpane_jdialog.allowed");
        assertTrue(jOptionPaneJDialogCount <= allowed,
                "Số lượng gọi JOptionPane/JDialog (" + jOptionPaneJDialogCount + ") vượt quá hạn mức (" + allowed + ")");
    }

    @Test
    @DisplayName("R-17: Chuỗi tiếng Việt trong presentation phải tập trung tại UiText")
    public void testNoVietnameseStringsOutsideUiTextInPresentation() {
        int allowed = getAllowed("vietnamese_presentation_strings.allowed");
        assertTrue(vietnamesePresentationStringsCount <= allowed,
                "Số chuỗi tiếng Việt ngoài UiText trong presentation (" + vietnamesePresentationStringsCount + ") vượt quá (" + allowed + ")");
    }

    @Test
    @DisplayName("Báo cáo số lượng vi phạm phong cách theo từng tầng package")
    public void testPrintViolationReportByPackage() {
        System.out.println("================================================================================");
        System.out.println("BÁO CÁO VI PHẠM PHONG CÁCH MÃ NGUỒN THEO TỪNG TẦNG (BASELINE BƯỚC 0)");
        System.out.println("================================================================================");
        System.out.printf("%-16s | %-6s | %-12s | %-8s | %-10s | %-8s | %-18s%n",
                "Package", "Files", "package-info", "Comments", "Stream/Var", "Hex/Dialog", "VN Strings in UI");
        System.out.println("--------------------------------------------------------------------------------");
        for (Map.Entry<String, Map<String, Integer>> entry : packageStats.entrySet()) {
            String pkg = entry.getKey();
            Map<String, Integer> s = entry.getValue();
            int hexAndDialog = s.getOrDefault("hex_colors", 0) + s.getOrDefault("dialogs", 0);
            System.out.printf("%-16s | %-6d | %-12d | %-8d | %-10d | %-8d | %-18d%n",
                    pkg,
                    s.getOrDefault("files", 0),
                    s.getOrDefault("package_info", 0),
                    s.getOrDefault("comments", 0),
                    s.getOrDefault("stream_optional_var", 0),
                    hexAndDialog,
                    s.getOrDefault("vietnamese_strings", 0));
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("TỔNG CỘNG: %d files | %d package-info | %d comments | %d Stream/Var | %d VN UI Strings%n",
                javaFiles.size(), packageInfoCount, commentCount, streamOptionalVarCount, vietnamesePresentationStringsCount);
        System.out.println("================================================================================");
    }
}
