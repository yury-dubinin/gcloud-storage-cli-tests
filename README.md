# Google Cloud Storage CLI Test Framework

A comprehensive automated testing framework for Google Cloud Storage CLI commands built with Java 17, Maven, TestNG, and Allure reporting.


## 📋 Tested Commands

1. **Sign URL** (`gcloud storage sign-url`) - Generate and validate signed URLs
2. **Create/Delete/List Buckets** (`gcloud storage buckets create/delete/list`) - Buckets operations
3. **Upload Operation** (`gcloud storage cp`) - Object Upload

## 🛠 Prerequisites

### Required Software
- **Java 21** or higher
- **Maven 3.6+**
- **Google Cloud CLI** (`gcloud`) installed and configured
- **Google Cloud Project** with IAM setup and service acount

### Authentication Setup
```bash
# Authenticate with Google Cloud
gcloud auth login

# Set your project (replace with your project ID)
gcloud config set project YOUR_PROJECT_ID

# Verify authentication
gcloud auth list
gcloud config list
```

### Storage Bucket Setup
The framework requires a Google Cloud Storage bucket for testing. You can either:
1. Set environment variable `GCS_TEST_BUCKET` to use an existing bucket
2. Let the framework auto-generate bucket names (recommended)

## 📦 Installation & Setup

### 1. Clone and Build
```bash
git clone <repository-url>
cd gcloud-storage-cli-tests
mvn clean compile
```

### 2. Configuration

#### Environment Variables (Recommended)

In order to run tests against specific version of gcloud you need to provide a `GCLOUD_PATH`

```bash
export GCLOUD_PATH="/path/to/gcloud"       # Optional
```

#### Configuration File
Edit `src/test/resources/config/test-config.yml`:
```yaml
gcloud:
  executable_path: "gcloud"

test:
  report_generation: true
  log_level: "INFO"
```

## 🚀 Running Tests

### Quick Start
```bash
# Run all tests
mvn clean test

# Run with specific test suite
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
```

### Test Categories

#### Run Critical Tests Only
```bash
mvn clean test -Dgroups="critical"
```

#### Run Individual Test Classes
```bash
# Sign URL tests
mvn clean test -Dtest=SignUrlCommandTest

# Sign URL test in Browser  
mvn clean test -Dtest=OpenSignUrlInBrowserTest

# List operation tests
mvn clean test -Dtest=BucketsListCommandTest

# Upload operation tests
mvn clean test -Dtest=UploadCommandTest

```

#### Run Performance Tests
```bash
mvn clean test -Dtest.suite=PerformanceTests
```

### Parallel Execution
```bash
# Run with custom thread count
mvn clean test -Dparallel.threads=2
```

## 📊 Test Reports

### Allure Reports
Generate and view detailed Allure reports:
```bash
# Generate Allure report
mvn allure:report

# Serve report locally
mvn allure:serve
```

<img width="1687" height="656" alt="Screenshot 2025-07-24 at 17 59 19" src="https://github.com/user-attachments/assets/fc5ce3bb-ef5e-4f46-ad9f-ac36be2274df" />


### TestNG Reports
Basic HTML reports are available at:
```
target/surefire-reports/index.html
```

## 🏗 Framework Architecture

### Core Components

#### 1. Configuration Management (`TestConfig.java`)
- YAML-based configuration loading
- Environment variable overrides
- Singleton pattern for global access

#### 2. Command Execution (`CommandExecutor.java`)
- gcloud command execution
- Process lifecycle management

#### 3. Storage Operations (`GcloudStorageOperations.java`)
- High-level wrapper for storage commands
- Type-safe result objects
- Built-in error handling

#### 4. Base Test Class (`BaseGcloudTest.java`)
- Common test infrastructure
- Automatic setup/teardown
- Utility methods and assertions

### Test Structure
```
src/test/java/com/google/cloud/testing/
├── base/
│   └── BaseGcloudTest.java           # Base test functionality
├── tests/
│   ├── SignUrlCommandTest.java       # Sign URL tests
│   ├── BucketsCreateCommandTest.java # Bucket create tests
│   ├── BucketsDeleteCommandTest.java # Bucket delete tests
│   ├── BucketsListCommandTest.java   # Bucket list tests
│   ├── UploadCommandTest.java        # Upload operation tests
│   └── OpenSignUrlInBrowserTest.java # Open signed URL in browser tests
├── utils/
│   └── NameGenerator.java            # Utility for generating unique names
└── resources/
    ├── testng.xml                    # TestNG suite configuration
    └── config/
        └── test-config.yml           # Test configuration
```

## 🔧 Customization

### Adding New Test Commands
1. Create new test class extending `BaseGcloudTest`
2. Add storage operation method to `GcloudStorageOperations`
3. Update TestNG suite configuration

Example:
```java
@Epic("GCloud Storage CLI")
@Feature("New Command")
public class NewCommandTest extends BaseGcloudTest {
    
    @Test
    @Story("Test new command")
    public void testNewCommand() {
        CommandResult result = storageOps.newOperation();
        assertSuccess(result, "New command test");
        // Add assertions
    }
}
```

## 🧪 Test Data Management

### Manual Cleanup
```bash
# Clean up specific bucket
gcloud storage rm -r gs://your-test-bucket

# List and clean up test buckets
gcloud storage ls | grep "gcloud-test-bucket" | xargs -I {} gcloud storage rm -r {}
```

## 📝 Logging

### Log Levels
Configure in `test-config.yml`:
```yaml
test:
  log_level: "INFO"  # TRACE, DEBUG, INFO, WARN, ERROR
```
