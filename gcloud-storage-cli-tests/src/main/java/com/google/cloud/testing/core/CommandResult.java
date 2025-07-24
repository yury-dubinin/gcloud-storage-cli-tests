package com.google.cloud.testing.core;

/**
 * Result of a command execution
 */
public class CommandResult {
    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final long executionTimeMs;
    private final boolean timedOut;

    public CommandResult(int exitCode, String stdout, String stderr, long executionTimeMs, boolean timedOut) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.executionTimeMs = executionTimeMs;
        this.timedOut = timedOut;
    }

    public boolean isSuccess() {
        return exitCode == 0 && !timedOut;
    }

    public int getExitCode() { return exitCode; }
    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public boolean isTimedOut() { return timedOut; }

    @Override
    public String toString() {
        return String.format("CommandResult{exitCode=%d, timedOut=%s, executionTime=%dms, stdout='%s', stderr='%s'}", 
                exitCode, timedOut, executionTimeMs, stdout.length() > 100 ? stdout.substring(0, 100) + "..." : stdout, 
                stderr.length() > 100 ? stderr.substring(0, 100) + "..." : stderr);
    }
}
