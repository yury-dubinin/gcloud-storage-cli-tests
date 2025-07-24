package com.google.cloud.testing.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.cloud.testing.base.BaseGcloudTest;
import com.google.cloud.testing.core.CommandResult;
import com.google.cloud.testing.storage.GcloudStorageOperations;
import com.google.cloud.testing.utils.NameGenerator;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Tests for gcloud storage ls (list) command Focus: Listing buckets and objects
 * with various options
 */
@Epic("GCloud Storage CLI")
@Feature("Upload Command")
public class UploadCommandTest extends BaseGcloudTest {

    private final String testBucketName = NameGenerator.generateBucketName();
    private final GcloudStorageOperations storageOps = new GcloudStorageOperations();
    private final String testLocation = "US";
    private File testFile;

    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        logger.info("Setting up test class: {}", this.getClass().getSimpleName());
        ensureTestBucketExists(testBucketName, testLocation, storageOps);
        logger.info("Test bucket {} is ready for upload tests", testBucketName);
        try {
            // Create temporary directory for test files
            tempTestDir = Files.createTempDirectory("gcloud-test-");
        } catch (IOException ex) {
            logger.error("Failed to create temporary directory for test files", ex);
            throw new RuntimeException("Could not create temporary directory for test files", ex);
        }

        // Create test files
        testFile = createTestFile(tempTestDir);

        logger.info("Created test file in {}", tempTestDir);
    }

    @Test(priority = 1)
    @Story("Upload local file")
    @Description("Test uploading a file to the existing bucket")
    @Severity(SeverityLevel.CRITICAL)
    public void testUploadFile() {
        logger.info("Running test with test bucket name: {}", testBucketName);

        CommandResult result = storageOps.uploadFile(testFile.getPath(), testBucketName);
        assertSuccess(result, "Upload file");

        String output = result.getStdout();
        logger.info("Upload File Output: {}", output);

        logger.info("Successfully uploaded file to bucket: {}", testBucketName);
        addAllureAttachment("Upload File Result", output);
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        logger.info("Tearing down test class: {}", this.getClass().getSimpleName());

        // Clean up test files
        if (testFile != null) {
            cleanupTestFiles(List.of(testFile));
        }

        // Clean up temporary directory
        if (tempTestDir != null) {
            try {
                Files.deleteIfExists(tempTestDir);
            } catch (IOException e) {
                logger.warn("Could not delete temp directory: {}", e.getMessage());
            }
        }
    }

}
