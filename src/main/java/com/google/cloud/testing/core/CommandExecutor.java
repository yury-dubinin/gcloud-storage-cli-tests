package com.google.cloud.testing.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.testing.config.TestConfig;

/**
 * Executes command line operations with proper error handling and timeouts
 */
public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    private final TestConfig config;

    public CommandExecutor() {
        this.config = TestConfig.getInstance();
    }

    /**
     * Execute a gcloud command with default timeout
     */
    public CommandResult executeGcloudCommand(String... args) {
        List<String> command = new ArrayList<>();
        command.add(config.getGcloud().getExecutablePath());
        command.addAll(Arrays.asList(args));
        
        return executeCommand(5000, command.toArray(String[]::new));
    }

    /**
     * Execute any system command with timeout
     */
    public CommandResult executeCommand(int timeoutSeconds, String... command) {
        logger.info("Executing command: {}", String.join(" ", command));
        long startTime = System.currentTimeMillis();

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);

        try {
            Process process = pb.start();

            // Read stdout and stderr in separate threads to prevent deadlock
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            Thread stdoutReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    logger.warn("Error reading stdout: {}", e.getMessage());
                }
            });

            Thread stderrReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    logger.warn("Error reading stderr: {}", e.getMessage());
                }
            });

            stdoutReader.start();
            stderrReader.start();

            // Wait for process to finish
            int exitCode = process.waitFor();

            // Wait for output readers to complete
            stdoutReader.join();
            stderrReader.join();

            long executionTime = System.currentTimeMillis() - startTime;

            logger.debug("Command completed with exit code: {}, execution time: {}ms", exitCode, executionTime);
            logger.debug("Stdout: {}", stdout.length() > 200 ? stdout.substring(0, 200) + "..." : stdout);
            logger.debug("Stderr: {}", stderr.length() > 200 ? stderr.substring(0, 200) + "..." : stderr);

            return new CommandResult(exitCode, stdout.toString(), stderr.toString(), executionTime, false);

        } catch (IOException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to execute command: {}", e.getMessage());
            return new CommandResult(-1, "", e.getMessage(), executionTime, false);
        } catch (InterruptedException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Command execution interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return new CommandResult(-1, "", e.getMessage(), executionTime, false);
        }
    }

    /**
     * Check if gcloud CLI is available and authenticated
     */
    public boolean isGcloudAvailable() {
        CommandResult result = executeCommand(10, config.getGcloud().getExecutablePath(), "version");
        return result.isSuccess();
    }

    /**
     * Check if user is authenticated with gcloud
     */
    public boolean isAuthenticated() {
        CommandResult result = executeCommand(10, config.getGcloud().getExecutablePath(), "auth", "list", "--filter=status:ACTIVE", "--format=value(account)");
        return result.isSuccess() && !result.getStdout().trim().isEmpty();
    }

    /**
     * Get current gcloud project
     */
    public String getCurrentProject() {
        CommandResult result = executeCommand(10, config.getGcloud().getExecutablePath(), "config", "get-value", "project");
        if (result.isSuccess()) {
            return result.getStdout().trim();
        }
        return null;
    }

    /**
     * Get gcloud version information
     */
    public String getGcloudVersion() {
        CommandResult result = executeCommand(10, config.getGcloud().getExecutablePath(), "version", "--format=json");
        if (result.isSuccess()) {
            var versionInfo = result.getStdout();
            try {
                // Parse JSON to extract "Google Cloud SDK" version
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, String> versionMap = objectMapper.readValue(versionInfo, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                return versionMap.getOrDefault("Google Cloud SDK", "unknown");
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                logger.error("Failed to parse gcloud version JSON: {}", e.getMessage());
            }
        }
        return "unknown";
    }
}
