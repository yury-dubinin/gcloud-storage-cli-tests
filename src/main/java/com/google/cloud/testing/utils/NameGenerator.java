package com.google.cloud.testing.utils;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for generating unique names.
 */
public class NameGenerator {

    private static final Logger logger = LoggerFactory.getLogger(NameGenerator.class);

    /**
     * Generate a unique test bucket name.
     */
    public static String generateBucketName() {
        String baseName = "mend-test-bucket";

        // Add UUID for uniqueness
        String uuid = UUID.randomUUID().toString().substring(0, 12);
        String testBucketName = String.format("%s-%s", baseName, uuid);
        logger.info("Generated test bucket name: {}", testBucketName);
        return testBucketName;
    }
}
