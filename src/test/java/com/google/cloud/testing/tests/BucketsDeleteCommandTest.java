package com.google.cloud.testing.tests;

import org.testng.Assert;
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

@Epic("GCloud Storage CLI")
@Feature("Delete Command")
public class BucketsDeleteCommandTest extends BaseGcloudTest {

    private final String testBucketName = NameGenerator.generateBucketName();
    private final GcloudStorageOperations storageOps = new GcloudStorageOperations();
    private final String testLocation = "US";

    @BeforeClass
    public void setupDeleteTests() {
        logger.info("Setting up delete command tests");
        ensureTestBucketExists(testBucketName, testLocation, storageOps);
    }

    @Test(priority = 1)
    @Story("Delete buckets")
    @Description("Test deleting a bucket in the project")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteBucket() {
        Assert.assertTrue(storageOps.bucketExists(testBucketName),
                "Bucket should be created!");
        CommandResult result = storageOps.deleteBucket(testBucketName);
        assertSuccess(result, "Delete bucket");

        String output = result.getStdout();
        logger.info("Delete Bucket Output: {}", output);

        logger.info("Successfully deleted bucket: {}", testBucketName);
        Assert.assertFalse(storageOps.bucketExists(testBucketName),
                "Bucket is not deleted");
        addAllureAttachment("Delete Bucket Result", output);
    }

}
