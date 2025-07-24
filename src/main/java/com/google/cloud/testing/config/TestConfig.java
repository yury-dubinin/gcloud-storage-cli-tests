package com.google.cloud.testing.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Configuration manager for test framework Handles loading configuration from
 * YAML files and environment variables
 */
public class TestConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);
    private static TestConfig instance;
    private static final Object lock = new Object();

    @JsonProperty("gcloud")
    private final GcloudConfig gcloudConfig = new GcloudConfig();

    @JsonProperty("test")
    private final TestSettings testSettings = new TestSettings();

    private TestConfig() {
        // Private constructor for singleton
    }

    public static TestConfig getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = loadConfiguration();
            }
        }
        return instance;
    }

    private static TestConfig loadConfiguration() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TestConfig config = new TestConfig();

        // Load from resources first
        try (InputStream is = TestConfig.class.getResourceAsStream("/config/test-config.yml")) {
            if (is != null) {
                config = mapper.readValue(is, TestConfig.class);
                logger.info("Loaded configuration from resources");
            }
        } catch (IOException e) {
            logger.warn("Could not load config from resources: {}", e.getMessage());
        }

        // Override with local config if exists
        File localConfig = new File("test-config.yml");
        if (localConfig.exists()) {
            try {
                config = mapper.readValue(localConfig, TestConfig.class);
                logger.info("Loaded configuration from local file: {}", localConfig.getAbsolutePath());
            } catch (IOException e) {
                logger.warn("Could not load local config: {}", e.getMessage());
            }
        }

        // Override with environment variables
        config.applyEnvironmentOverrides();
        return config;
    }

    private void applyEnvironmentOverrides() {
        String gcloudPath = System.getenv("GCLOUD_PATH");
        if (gcloudPath != null) {
            gcloudConfig.setExecutablePath(gcloudPath);
        }
    }

    public GcloudConfig getGcloud() {
        return gcloudConfig;
    }

    public TestSettings getTest() {
        return testSettings;
    }

    public static class GcloudConfig {

        @JsonProperty("executable_path")
        private String executablePath;

        @JsonProperty("service_account")
        private String serviceAccount;

        public String getExecutablePath() {
            return executablePath;
        }

        public void setExecutablePath(String executablePath) {
            this.executablePath = executablePath;
        }

        public String getServiceAccount() {
            return serviceAccount;
        }
    }

    public static class TestSettings {


        @JsonProperty("report_generation")
        private boolean reportGeneration = true;

        @JsonProperty("log_level")
        private String logLevel = "INFO";

        public boolean isReportGeneration() {
            return reportGeneration;
        }

        public void setReportGeneration(boolean reportGeneration) {
            this.reportGeneration = reportGeneration;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }
    }
}
