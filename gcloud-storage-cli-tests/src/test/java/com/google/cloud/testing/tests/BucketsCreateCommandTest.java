package com.google.cloud.testing.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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

@Epic("GCloud Storage CLI")
@Feature("Create Command")
public class BucketsCreateCommandTest extends BaseGcloudTest {

    protected static final Logger logger = LoggerFactory.getLogger(BucketsCreateCommandTest.class);
    private final GcloudStorageOperations storageOps = new GcloudStorageOperations();
    private final String testBucketName = NameGenerator.generateBucketName();
    private final String testLocation = "US";

    @Test(priority = 1)
    @Story("Create buckets")
    @Description("Test creating a new bucket in the project")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateBucket() {
        logger.info("Running create command test...");
        CommandResult result = storageOps.createBucket(testBucketName, testLocation);
        assertSuccess(result, "Create bucket");

        String output = result.getStdout();
        logger.info("Create Bucket Output: {}", output);

        logger.info("Successfully created bucket: {}", testBucketName);
        Assert.assertTrue(storageOps.bucketExists(testBucketName),
                "Bucket is not created");
        addAllureAttachment("Create Bucket Result", output);
    }

    @AfterClass(alwaysRun = true)
    public void teardownCreateTests() {
        logger.info("Tearing down test class: {}", this.getClass().getSimpleName());

        if (testBucketName != null) {
            try {
                if (storageOps.bucketExists(testBucketName)) {
                    // Then delete the bucket
                    storageOps.deleteBucket(testBucketName);
                    logger.info("Deleted test bucket: {}", testBucketName);
                }
            } catch (Exception e) {
                logger.warn("Could not delete test bucket {}: {}", testBucketName, e.getMessage());
            }
        }
    }

}
