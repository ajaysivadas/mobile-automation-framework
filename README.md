# Mobile App Automation Framework

A cross-platform mobile app automation framework built with **Java 17**, **Appium**, **Selenide**, **TestNG**, and **Allure** reporting. Supports both **Android** and **iOS** platforms with thread-safe parallel execution.

## Architecture

```
src/
├── main/java/AppAutomation/
│   ├── Base/                    # Core framework classes
│   │   ├── DriverManager.java       # Thread-safe Appium driver management
│   │   ├── AppiumServer.java        # Appium server lifecycle management
│   │   ├── DesiredCaps.java         # Platform-specific capabilities
│   │   ├── MobileActions.java       # Click, input, tap, swipe, scroll
│   │   ├── MobileVerifications.java # Element state verification
│   │   ├── NativeActions.java       # Device-native actions (back, home, rotate)
│   │   └── WaitManager.java         # Implicit, explicit, and fluent waits
│   ├── Assertions/              # Enhanced TestNG assertions with Allure + screenshots
│   ├── Listener/                # TestNG listeners for Allure integration
│   ├── Screens/                 # Page Object Model (screen classes)
│   │   └── LogIn/
│   │       └── LoginScreen.java
│   ├── TestData/                # Centralized test configuration
│   └── Utils/                   # Utilities
│       ├── ElementHandler.java      # Cross-platform element locator
│       ├── AllureManager.java       # Allure report utilities
│       ├── Log.java                 # Thread-safe logging
│       ├── SecretsHandler.java      # Config file reader
│       └── CheckRunEnvironment.java # GitHub Actions vs local detection
├── main/java/Resources/
│   └── config-stage.properties  # Local configuration (not committed)
└── test/java/AppAutomation/
    └── Login/
        └── LoginScreenTest.java # Sample test class

test-suite/
└── testng-Login.xml             # TestNG suite configuration
```

## Prerequisites

- **Java 17** (JDK)
- **Maven** 3.6+
- **Appium** 2.x (`npm install -g appium`)
- **Node.js** 18+
- **Android SDK** (for Android testing) or **Xcode** (for iOS testing)
- Android emulator or physical device connected

## Setup

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd App-Automation
   ```

2. **Configure your environment**

   Copy and edit the config file:
   ```bash
   cp src/main/java/Resources/config-stage.properties.example src/main/java/Resources/config-stage.properties
   ```

   Update these values in `config-stage.properties`:
   ```properties
   platform=android
   apkApp=/absolute/path/to/your/app.apk
   deviceNameAndroid=Your_Emulator_Name
   androidPackage=com.your.app.package
   ```

3. **Install Appium and drivers**
   ```bash
   npm install -g appium
   appium driver install uiautomator2   # Android
   appium driver install xcuitest       # iOS
   ```

4. **Install Maven dependencies**
   ```bash
   mvn clean install
   ```

## Running Tests

```bash
# Run by Maven profile
mvn test -P Login

# Run a specific test class
mvn test -Dtest=LoginScreenTest

# Run with platform override
mvn test -P Login -Dplatform=ios
```

## Allure Reports

```bash
# Generate report
mvn allure:report

# Generate and open in browser
mvn allure:serve
```

## Key Concepts

### Page Object Model
Screen classes in `Screens/` represent app screens. Each screen:
- Accepts `AppiumDriver` in constructor
- Declares elements using `ElementHandler.getElement()`
- Exposes action methods (e.g., `clickSubmitButton()`, `isPageVisible()`)

### Cross-Platform Element Handling
`ElementHandler` auto-detects locator type from the string:
- `//xpath` or `(//xpath)` → XPath
- `android.widget.*` or `XCUIElementType*` → Class Name
- Everything else → Accessibility ID

Use `getElement(androidLocator, iosLocator)` for platform-specific locators.

### Test Structure
```java
@BeforeClass: DriverManager.initializeDriver() → get driver → instantiate screens
@Test(priority=N): Test methods with @Description and @Severity
@AfterClass: DriverManager.quitDriver()
```

### Adding a New Test Suite
1. Create screen class(es) in `Screens/`
2. Create test class(es) in `src/test/java/`
3. Create TestNG XML in `test-suite/`
4. Add Maven profile in `pom.xml`

## Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Language |
| Appium | 8.6.0 | Mobile automation |
| Selenide | 7.10.0 | UI framework |
| TestNG | 7.7.0 | Test framework |
| Allure | 2.25.0 | Reporting |
| Maven | 3.6+ | Build tool |
