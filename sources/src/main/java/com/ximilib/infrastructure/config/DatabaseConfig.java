package com.ximilib.infrastructure.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * DatabaseConfig: Nạp cấu hình kết nối CSDL MySQL và khóa bảo mật tập trung (R-07, R-08).
 * Thứ tự ưu tiên cấu hình:
 * 1. Biến môi trường hệ thống (XIMILIB_DB_*, XIMILIB_AES_KEY, MYSQL_PWD)
 * 2. File cấu hình config/db.properties (hoặc ../config/db.properties)
 * Tuyệt đối không lưu cứng mật khẩu hoặc khóa bảo mật mặc định trong mã nguồn.
 */
public class DatabaseConfig {

    private static final String CONFIG_FILE_NAME = "config/db.properties";

    private final String host;
    private final String port;
    private final String dbName;
    private final String user;
    private final String password;
    private final String aesKey;

    private final String poolName;
    private final int maxPoolSize;
    private final int minIdle;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;
    private final long leakDetectionThreshold;

    private static volatile DatabaseConfig instance;
    private static final Object lock = new Object();

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    public static void resetInstance() {
        synchronized (lock) {
            instance = null;
        }
    }

    private DatabaseConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.dbName = builder.dbName;
        this.user = builder.user;
        this.password = builder.password;
        this.aesKey = builder.aesKey;
        this.poolName = builder.poolName;
        this.maxPoolSize = builder.maxPoolSize;
        this.minIdle = builder.minIdle;
        this.connectionTimeout = builder.connectionTimeout;
        this.idleTimeout = builder.idleTimeout;
        this.maxLifetime = builder.maxLifetime;
        this.leakDetectionThreshold = builder.leakDetectionThreshold;
    }

    public static DatabaseConfig load() {
        Properties fileProps = loadConfigFile();

        String host = getEnvOrProp("XIMILIB_DB_HOST", fileProps, "db.host", "localhost");
        String port = getEnvOrProp("XIMILIB_DB_PORT", fileProps, "db.port", "3306");
        String dbName = getEnvOrProp("XIMILIB_DB_NAME", fileProps, "db.name", "ximilib_db");

        String user = getEnvOrProp("XIMILIB_DB_USER", fileProps, "db.user", null);
        String pass = System.getenv("XIMILIB_DB_PASS");
        if (pass == null || pass.trim().isEmpty()) {
            pass = System.getenv("MYSQL_PWD");
        }
        if (pass == null || pass.trim().isEmpty()) {
            pass = fileProps.getProperty("db.password");
        }

        String aes = System.getenv("XIMILIB_AES_KEY");
        if (aes == null || aes.trim().isEmpty()) {
            aes = fileProps.getProperty("security.aes.key");
        }

        String poolName = getEnvOrProp("XIMILIB_DB_POOL_NAME", fileProps, "db.pool.name", "XimiLib-HikariPool");
        int maxPoolSize = parseInt(getEnvOrProp("XIMILIB_DB_MAX_POOL", fileProps, "db.pool.max-size", "10"), 10);
        int minIdle = parseInt(getEnvOrProp("XIMILIB_DB_MIN_IDLE", fileProps, "db.pool.min-idle", "2"), 2);
        long connTimeout = parseLong(getEnvOrProp("XIMILIB_DB_CONN_TIMEOUT", fileProps, "db.pool.connection-timeout", "5000"), 5000L);
        long idleTimeout = parseLong(getEnvOrProp("XIMILIB_DB_IDLE_TIMEOUT", fileProps, "db.pool.idle-timeout", "60000"), 60000L);
        long maxLifetime = parseLong(getEnvOrProp("XIMILIB_DB_MAX_LIFETIME", fileProps, "db.pool.max-lifetime", "1800000"), 1800000L);
        long leakThreshold = parseLong(getEnvOrProp("XIMILIB_DB_LEAK_THRESHOLD", fileProps, "db.pool.leak-detection-threshold", "5000"), 5000L);

        Builder builder = new Builder()
                .host(host)
                .port(port)
                .dbName(dbName)
                .user(user)
                .password(pass)
                .aesKey(aes)
                .poolName(poolName)
                .maxPoolSize(maxPoolSize)
                .minIdle(minIdle)
                .connectionTimeout(connTimeout)
                .idleTimeout(idleTimeout)
                .maxLifetime(maxLifetime)
                .leakDetectionThreshold(leakThreshold);

        return new DatabaseConfig(builder);
    }

    private static Properties loadConfigFile() {
        Properties props = new Properties();
        File configFile = resolveConfigFile();
        if (configFile != null && configFile.exists() && configFile.canRead()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (Exception ignored) {
                // Tiếp tục đọc từ biến môi trường nếu không đọc được file
            }
        }
        return props;
    }

    private static File resolveConfigFile() {
        String explicitPath = System.getProperty("ximilib.config.path");
        if (explicitPath != null && !explicitPath.isBlank()) {
            File f = new File(explicitPath);
            if (f.exists()) return f;
        }

        File direct = new File(CONFIG_FILE_NAME);
        if (direct.exists()) return direct;

        File parentRelative = new File("../" + CONFIG_FILE_NAME);
        if (parentRelative.exists()) return parentRelative;

        return null;
    }

    private static String getEnvOrProp(String envKey, Properties props, String propKey, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        String propVal = props.getProperty(propKey);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return propVal.trim();
        }
        return defaultValue;
    }

    private static int parseInt(String val, int defaultVal) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static long parseLong(String val, long defaultVal) {
        try {
            return Long.parseLong(val);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public String getHost() { return host; }
    public String getPort() { return port; }
    public String getDbName() { return dbName; }

    public String getUser() {
        if (user == null || user.trim().isEmpty()) {
            throw new IllegalStateException("Thiếu cấu hình tài khoản kết nối CSDL! Vui lòng thiết lập biến môi trường XIMILIB_DB_USER hoặc khai báo 'db.user' trong file config/db.properties.");
        }
        return user.trim();
    }

    public String getPassword() {
        if (password == null) {
            throw new IllegalStateException("Thiếu cấu hình mật khẩu kết nối CSDL! Vui lòng thiết lập biến môi trường XIMILIB_DB_PASS (hoặc MYSQL_PWD) hoặc khai báo 'db.password' trong file config/db.properties.");
        }
        return password;
    }

    public String getAesKey() {
        if (aesKey == null || aesKey.trim().isEmpty()) {
            throw new IllegalStateException("Thiếu cấu hình khóa mã hóa AES! Vui lòng thiết lập biến môi trường XIMILIB_AES_KEY hoặc khai báo 'security.aes.key' trong file config/db.properties.");
        }
        return aesKey.trim();
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    public String getPoolName() { return poolName; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public int getMinIdle() { return minIdle; }
    public long getConnectionTimeout() { return connectionTimeout; }
    public long getIdleTimeout() { return idleTimeout; }
    public long getMaxLifetime() { return maxLifetime; }
    public long getLeakDetectionThreshold() { return leakDetectionThreshold; }

    public static class Builder {
        private String host = "localhost";
        private String port = "3306";
        private String dbName = "ximilib_db";
        private String user;
        private String password;
        private String aesKey;
        private String poolName = "XimiLib-HikariPool";
        private int maxPoolSize = 10;
        private int minIdle = 2;
        private long connectionTimeout = 5000L;
        private long idleTimeout = 60000L;
        private long maxLifetime = 1800000L;
        private long leakDetectionThreshold = 5000L;

        public Builder host(String host) { this.host = host; return this; }
        public Builder port(String port) { this.port = port; return this; }
        public Builder dbName(String dbName) { this.dbName = dbName; return this; }
        public Builder user(String user) { this.user = user; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder aesKey(String aesKey) { this.aesKey = aesKey; return this; }
        public Builder poolName(String poolName) { this.poolName = poolName; return this; }
        public Builder maxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; return this; }
        public Builder minIdle(int minIdle) { this.minIdle = minIdle; return this; }
        public Builder connectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; return this; }
        public Builder idleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; return this; }
        public Builder maxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; return this; }
        public Builder leakDetectionThreshold(long leakDetectionThreshold) { this.leakDetectionThreshold = leakDetectionThreshold; return this; }

        public DatabaseConfig build() { return new DatabaseConfig(this); }
    }
}
