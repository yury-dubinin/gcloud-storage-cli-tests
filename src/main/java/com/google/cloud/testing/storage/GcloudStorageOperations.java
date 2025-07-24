package com.google.cloud.testing.storage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.testing.config.TestConfig;
import com.google.cloud.testing.core.CommandExecutor;
import com.google.cloud.testing.core.CommandResult;

/**
 * Wrapper for Google Cloud Storage operations using gcloud CLI
 */
public class GcloudStorageOperations {

    private static final Logger logger = LoggerFactory.getLogger(GcloudStorageOperations.class);
    private final CommandExecutor executor;
    protected TestConfig config = TestConfig.getInstance();

    public GcloudStorageOperations() {
        this.executor = new CommandExecutor();
    }

    /**
     * Create a bucket
     */
    public CommandResult createBucket(String bucketName, String location) {
        if(bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        logger.info("Creating bucket: {} in location: {}", bucketName, location);
        return executor.executeGcloudCommand(
                "storage", "buckets", "create", "gs://" + bucketName,
                "--location=" + location
        );
    }

    /**
     * Delete a bucket
     */
    public CommandResult deleteBucket(String bucketName) {
        if(bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        logger.info("Deleting bucket: {}", bucketName);
        return executor.executeGcloudCommand(
                "storage", "buckets", "delete", "gs://" + bucketName
        );
    }

    /**
     * List buckets in the project. Returns a CommandResult with JSON output.
     */
    public CommandResult listBuckets() {
        logger.info("Listing buckets");
        return executor.executeGcloudCommand(
                "storage", "buckets", "list", "--format=json(name)"
        );
    }

    /**
     * Check if bucket exists
     */
    public boolean bucketExists(String bucketName) {
        CommandResult result = executor.executeGcloudCommand(
                "storage", "buckets", "describe", "gs://" + bucketName, "--format=value(name)"
        );
        return result.isSuccess();
    }

    /**
     * Describe a Cloud Storage bucket.
     */
    public String bucketDescribe(String bucketName) {
        CommandResult result = executor.executeGcloudCommand(
                "storage", "buckets", "describe", "gs://" + bucketName, "--format=value(name)"
        );
        return result.getStdout();
    }

    /**
     * Upload a file to bucket
     */
    public CommandResult uploadFile(String localFilePath, String bucketName) {
        logger.info("Uploading file {} to gs://{}", localFilePath, bucketName);
        return executor.executeGcloudCommand(
                "storage", "cp", localFilePath, "gs://" + bucketName
        );
    }

    /**
     * List objects in a bucket
     */
    public CommandResult listObjects(String bucketName) {
        logger.info("Listing objects in bucket: {}", bucketName);
        return executor.executeGcloudCommand(
                "storage", "ls", "gs://" + bucketName
        );
    }

    /**
     * Generate a signed URL for an object
     */
    public List<SignedUrlOutput> generateSignedUrl(String fileUrlString, Duration duration) {
        logger.info("Generating signed URL for {} with duration: {}", fileUrlString, duration);

        List<String> args = new ArrayList<>();
        args.add("storage");
        args.add("sign-url");
        args.add(fileUrlString);
        args.add("--duration=" + duration.toSeconds() + "s");
        args.add("--impersonate-service-account=" + config.getGcloud().getServiceAccount());
        args.add("--format=json");

        CommandResult result = executor.executeGcloudCommand(
                args.toArray(String[]::new)
        );

        if (result.isSuccess()) {
            return mapSignedUrlFromOutput(result.getStdout());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Extract signed URL from gcloud command output
     */
    private List<SignedUrlOutput> mapSignedUrlFromOutput(String output) {
        logger.info("Extracting signed URLs from output: {}", output);
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Parse JSON array of objects into a List of Maps
            List<SignedUrlOutput> list3 = mapper.readValue(output, new TypeReference<List<SignedUrlOutput>>() {
            });
            logger.info("Extracted signed URLs: {}", list3.get(0).signed_url());
            return list3;
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse JSON output: {}", output, ex);
        }

        logger.warn("Could not extract signed URLs from output: {}", output);
        return null;
    }

    /**
     * Result of signed URL generation
     */
    public record SignedUrlOutput(String expiration, String http_verb, String resource, String signed_url) {

    }

    public record SignedUrlOutputList(List<SignedUrlOutput> signedUrls) {

    }
}
