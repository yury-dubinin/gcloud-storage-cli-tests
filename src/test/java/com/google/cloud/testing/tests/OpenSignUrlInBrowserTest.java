package com.google.cloud.testing.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.cloud.testing.base.BaseGcloudTest;
import com.google.cloud.testing.core.CommandResult;
import com.google.cloud.testing.storage.GcloudStorageOperations;
import com.google.cloud.testing.utils.NameGenerator;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Tests for gcloud storage sign-url command Focus: Validate signed URL in
 * Browser generation and format
 */
@Epic("GCloud Storage CLI")
@Feature("Sign URL Command")
public class OpenSignUrlInBrowserTest extends BaseGcloudTest {

    private final String testBucketName = NameGenerator.generateBucketName();
    private String uploadedFileNamePath;
    private File testFile;
    private final GcloudStorageOperations storageOps = new GcloudStorageOperations();
    private final String testLocation = "US";

    @BeforeClass
    public void setupSignUrlTests() {
        logger.info("Setting up sign-url command tests");
        ensureTestBucketExists(testBucketName, testLocation, storageOps);
        // Create and upload test files
        try {
            // Create temporary directory for test files
            tempTestDir = Files.createTempDirectory("gcloud-test");
        } catch (IOException ex) {
            logger.error("Failed to create temporary directory for test files", ex);
            throw new RuntimeException("Could not create temporary directory for test files", ex);
        }

        // Create test files
        testFile = createTestFile(tempTestDir);
        logger.info("Test bucket {} is ready for upload tests", testBucketName);
        // Prepare uploaded file path
        uploadedFileNamePath = "gs://" + testBucketName + "/" + testFile.getName();
        Assert.assertNotNull(uploadedFileNamePath, "No test files were created");
        CommandResult result = storageOps.uploadFile(testFile.getPath(), testBucketName);
        assertSuccess(result, "Upload file");

        logger.info("Sign-url tests setup complete with uploaded file: {}", uploadedFileNamePath);
    }

    @Test(priority = 1)
    @Story("Basic signed URL generation")
    @Description("Test basic signed URL generation for different file types")
    @Severity(SeverityLevel.CRITICAL)
    public void testSignedUrlInBrowser() {
        logger.info("Testing basic signed URL generation");

        Duration duration = Duration.ofHours(1);

        // Generate signed URL
        var result
                = storageOps.generateSignedUrl(uploadedFileNamePath, duration).get(0);

        // Verify command success
        Assert.assertNotNull(result, "Sign URL generation for " + uploadedFileNamePath);

        // Verify resource URL
        Assert.assertEquals(result.resource(), uploadedFileNamePath);
        // Verify URL was extracted
        Assert.assertTrue(result.signed_url().startsWith("https://storage.googleapis.com"),
                "Generated URL does not start with expected domain");
        Assert.assertTrue(result.signed_url().contains(uploadedFileNamePath.replace("gs://", "")),
                "Generated URL does not contain uploaded file: " + uploadedFileNamePath);

        // Add URL to Allure report
        addAllureAttachment("Signed URL for " + uploadedFileNamePath, result.signed_url());

        // Open the signed URL in a browser
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(150));
            Page page = browser.newPage();
            page.navigate(result.signed_url());
            boolean isVisible = page.getByText("This is a test file for GCloud Storage CLI testing.").isVisible();
            Assert.assertTrue(isVisible, "The content of the test file should be visible in the browser");
            var screenshot = page.screenshot();
            attachScreenshotToAllureReport("Signed URL Screenshot", screenshot);
        } catch (Exception e) {
            logger.error("Failed to open signed URL in browser", e);
            Assert.fail("Could not open signed URL in browser: " + e.getMessage());
        }
    }

}
