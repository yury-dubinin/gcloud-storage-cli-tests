package com.google.cloud.testing.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.google.cloud.testing.config.TestConfig;
import com.google.cloud.testing.core.CommandExecutor;
import com.google.cloud.testing.core.CommandResult;
import com.google.cloud.testing.storage.GcloudStorageOperations;

import io.qameta.allure.Allure;

/**
 * Base test class providing common functionality for all GCloud Storage CLI
 * tests
 */
public abstract class BaseGcloudTest {

    protected static final Logger logger = LoggerFactory.getLogger(BaseGcloudTest.class);
    protected TestConfig config;
    protected CommandExecutor executor;
    protected Path tempTestDir;

    @BeforeSuite(alwaysRun = true)
    public void setupSuite() {
        logger.info("Starting GCloud Storage CLI Test Suite");

        // Initialize configuration
        config = TestConfig.getInstance();
        // Log test environment info
        logEnvironmentInfo();

        // Verify gcloud CLI availability
        executor = new CommandExecutor();
        if (!executor.isGcloudAvailable()) {
            throw new RuntimeException("gcloud CLI is not available or not in PATH");
        }

        if (!executor.isAuthenticated()) {
            throw new RuntimeException("gcloud CLI is not authenticated. Please run 'gcloud auth login'");
        }

        logger.info("GCloud CLI verification completed successfully");
        logger.info("GCloud version: {}", executor.getGcloudVersion());
        logger.info("Current project: {}", executor.getCurrentProject());
    }

    @AfterSuite(alwaysRun = true)
    public void teardownSuite() {
        logger.info("GCloud Storage CLI Test Suite completed");
    }

    /**
     * Log environment information
     */
    private void logEnvironmentInfo() {
        logger.info("=== Test Environment Information ===");
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        logger.info("User: {}", System.getProperty("user.name"));
        logger.info("Working directory: {}", System.getProperty("user.dir"));

        // Log relevant environment variables
        String[] envVars = {"GCP_PROJECT_ID", "GCLOUD_PATH"};
        for (String var : envVars) {
            String value = System.getenv(var);
            logger.info("Environment variable {}: {}", var, value != null ? value : "not set");
        }
        logger.info("=====================================");
    }

    /**
     * Attach text to Allure report
     */
    protected void attachTextToAllureReport(String name, String content) {
        if (content != null && !content.isEmpty()) {
            Allure.addAttachment(name, "text/plain", content);
        }
    }

    /**
     * Attach screenshot to Allure report
     */
    protected void attachScreenshotToAllureReport(String name, byte[] screenshot) {
        if (screenshot != null && screenshot.length > 0) {
            Allure.addAttachment(name, "image/png", new java.io.ByteArrayInputStream(screenshot), "png");
        }
    }

    /**
     * Attach screenshot from file to Allure report
     */
    protected void attachScreenshotFromFileToAllureReport(String name, Path screenshotPath) {
        try {
            if (Files.exists(screenshotPath)) {
                byte[] screenshot = Files.readAllBytes(screenshotPath);
                attachScreenshotToAllureReport(name, screenshot);
            } else {
                logger.warn("Screenshot file not found: {}", screenshotPath);
            }
        } catch (IOException e) {
            logger.error("Failed to read screenshot file: {}", screenshotPath, e);
        }
    }

    /**
     * Ensure test bucket exists
     */
    protected void ensureTestBucketExists(String bucketName, String location, GcloudStorageOperations _storageOps) {
        if (!_storageOps.bucketExists(bucketName)) {
            var result = _storageOps.createBucket(bucketName, location);
            if (!result.isSuccess()) {
                throw new RuntimeException("Failed to create test bucket: " + result.getStderr());
            }
            logger.info("Created test bucket: {}", bucketName);
        }
        var output = _storageOps.bucketDescribe(bucketName);
        logger.info("Test bucket {} description: {}", bucketName, output);
    }

    /**
     * Assert that a command result was successful
     */
    protected void assertSuccess(CommandResult result, String operation) {
        if (!result.isSuccess()) {
            String errorMessage = String.format("%s failed. Exit code: %d, Error: %s",
                    operation, result.getExitCode(), result.getStderr());

            // Add failure details to Allure report
            attachTextToAllureReport("Command Error", errorMessage);
            attachTextToAllureReport("Command Output", result.getStdout());

            Assert.fail(errorMessage);
        }
    }

    /**
     * Add text attachment to Allure report
     */
    protected void addAllureAttachment(String name, String content) {
        attachTextToAllureReport(name, content);
    }

    /**
     * Generate a unique object name for testing
     */
    protected String generateUniqueObjectName(String prefix) {
        return String.format("%s-%d-%s", prefix, System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Create test files for upload testing
     */
    public File createTestFile(Path tempDir) {
        File testFile = new File(tempDir.toFile(), "test-file.txt");
        try {
            String content = "This is a test file for GCloud Storage CLI testing.";
            Files.write(testFile.toPath(), content.getBytes());
            logger.info("Created test file: {} (size: {} bytes)", testFile.getAbsolutePath(), Files.size(testFile.toPath()));
        } catch (IOException e) {
            logger.error("Failed to create test file", e);
        }
        return testFile;
    }

    /**
     * Clean up test files
     */
    public void cleanupTestFiles(List<File> files) {
        files.forEach(file -> {
            if (file.exists() && file.delete()) {
                logger.debug("Deleted test file: {}", file.getAbsolutePath());
            }
        });
    }
}
