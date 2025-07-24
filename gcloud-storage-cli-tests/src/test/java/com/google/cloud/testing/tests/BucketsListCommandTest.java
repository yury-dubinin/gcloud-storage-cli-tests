package com.google.cloud.testing.tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.cloud.testing.base.BaseGcloudTest;
import com.google.cloud.testing.core.CommandResult;
import com.google.cloud.testing.storage.GcloudStorageOperations;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("GCloud Storage CLI")
@Feature("List Command")
public class BucketsListCommandTest extends BaseGcloudTest {

    private final String testBucketName = "mend-test-466";;
    private final GcloudStorageOperations storageOps = new GcloudStorageOperations();
    private final String testLocation = "US";

    @BeforeClass
    public void setupListTests() {
        logger.info("Setting up list command tests");
        // Get or create a test bucket name
        ensureTestBucketExists(testBucketName, testLocation, storageOps);
    }

    @Test(priority = 1)
    @Story("List buckets")
    @Description("Test listing all buckets in the project")
    @Severity(SeverityLevel.CRITICAL)
    public void testListBuckets() {
        logger.info("Testing list buckets");
        CommandResult result = storageOps.listBuckets();
        assertSuccess(result, "List buckets");

        String output = result.getStdout();
        logger.info("List Buckets Output: {}", output);
        Assert.assertTrue(output.contains(testBucketName),
                "Bucket list should contain test bucket: " + testBucketName);

        logger.info("Successfully listed buckets. Found test bucket: {}", testBucketName);
        addAllureAttachment("List Buckets Result", output);
    }

}
