# Building a Cross-Platform Mobile App Automation Framework from Scratch

A detailed guide on how this framework was designed, built, and structured — covering every architectural decision, tool choice, and implementation pattern.

---

## Table of Contents

1. [Why Build a Custom Framework?](#1-why-build-a-custom-framework)
2. [Tech Stack — What and Why](#2-tech-stack--what-and-why)
3. [Project Structure](#3-project-structure)
4. [Setting Up the Foundation](#4-setting-up-the-foundation)
   - [Maven Project Setup](#41-maven-project-setup)
   - [Configuration Management](#42-configuration-management)
   - [Environment Detection](#43-environment-detection)
5. [Building the Core Engine](#5-building-the-core-engine)
   - [Appium Server Management](#51-appium-server-management)
   - [Desired Capabilities](#52-desired-capabilities)
   - [Driver Manager](#53-driver-manager)
6. [Cross-Platform Element Handling](#6-cross-platform-element-handling)
7. [Mobile Interactions Layer](#7-mobile-interactions-layer)
   - [MobileActions](#71-mobileactions)
   - [MobileVerifications](#72-mobileverifications)
   - [NativeActions](#73-nativeactions)
   - [WaitManager](#74-waitmanager)
8. [Page Object Model — Screen Classes](#8-page-object-model--screen-classes)
9. [Assertions with Auto-Screenshots](#9-assertions-with-auto-screenshots)
10. [Test Structure and TestNG Integration](#10-test-structure-and-testng-integration)
11. [Allure Reporting](#11-allure-reporting)
12. [Logging](#12-logging)
13. [Running Tests](#13-running-tests)
14. [Extending the Framework](#14-extending-the-framework)
15. [CI/CD Integration](#15-cicd-integration)

---

## 1. Why Build a Custom Framework?

Most mobile automation projects start with raw Appium calls scattered across test files. This works for 5 tests. It falls apart at 50. The problems:

- **Duplicate code** — the same element-finding, clicking, and waiting logic everywhere
- **Platform lock-in** — Android-specific code that breaks when you add iOS
- **Brittle tests** — one UI change breaks dozens of tests
- **No reporting** — you only know a test failed, not *why* or *where*
- **No parallel execution** — tests run one at a time because they share a single driver

This framework solves all of these by creating clear layers of abstraction:

```
Tests → Screen Objects → Mobile Actions → Element Handler → Appium Driver
```

Each layer only talks to the one below it. A UI change? Update one screen class. Adding iOS? Just add iOS locators to `ElementHandler.getElement()`. Need parallel execution? `ThreadLocal` storage handles it automatically.

---

## 2. Tech Stack — What and Why

| Technology | Version | Why This One? |
|-----------|---------|--------------|
| **Java 17** | 17 | Strong typing catches bugs at compile time. Excellent Appium client support. Mature ecosystem. |
| **Appium** | 8.6.0 | The industry standard for cross-platform mobile automation. One API for Android + iOS. |
| **Selenide** | 7.10.0 | Wraps Selenium/Appium with smart waits, fluent API, and automatic screenshots. Eliminates most `WebDriverWait` boilerplate. |
| **TestNG** | 7.7.0 | More powerful than JUnit for test automation — supports `priority`, `dependsOnMethods`, parallel execution, suite XML files, and listeners. |
| **Allure** | 2.25.0 | Beautiful, interactive test reports with steps, screenshots, and history. Integrates natively with TestNG. |
| **Maven** | 3.6+ | Standard Java build tool. Profiles map cleanly to test suites. |
| **Jackson** | 2.16.1 | JSON parsing for API responses and config files. |
| **Log4j** | 2.23.1 | Production-grade logging with file + console output. |

**Why Selenide over raw Appium?**

Raw Appium requires you to manually wait for elements, handle stale references, and manage timeouts. Selenide wraps all of this:

```java
// Raw Appium — verbose, fragile
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn")));
element.click();

// Selenide — concise, auto-waits
element.shouldBe(Condition.visible).click();
```

Selenide's `shouldBe()` automatically retries until the condition is met or times out. This single change eliminates 80% of flaky test failures.

---

## 3. Project Structure

```
src/
├── main/java/AppAutomation/
│   ├── Base/                        # The engine
│   │   ├── DriverManager.java           # Thread-safe driver lifecycle
│   │   ├── AppiumServer.java            # Server start/stop/port management
│   │   ├── DesiredCaps.java             # Android/iOS capabilities
│   │   ├── MobileActions.java           # All UI interactions
│   │   ├── MobileVerifications.java     # Element state checks
│   │   ├── NativeActions.java           # Device-level actions
│   │   └── WaitManager.java            # Wait strategies
│   ├── Assertions/                  # Enhanced assertions
│   │   └── Assertions.java
│   ├── Listener/                    # TestNG event hooks
│   │   └── TestListener.java
│   ├── Screens/                     # Page Objects
│   │   └── LogIn/
│   │       └── LoginScreen.java
│   ├── TestData/                    # Configuration constants
│   │   └── TestData.java
│   └── Utils/                       # Shared utilities
│       ├── ElementHandler.java          # Cross-platform locator
│       ├── AllureManager.java           # Report utilities
│       ├── Log.java                     # Custom logger
│       ├── SecretsHandler.java          # Config file reader
│       └── CheckRunEnvironment.java     # GitHub Actions detection
├── main/java/Resources/
│   └── config-stage.properties      # Environment config
└── test/java/AppAutomation/
    └── Login/
        └── LoginScreenTest.java     # Test classes

test-suite/
└── testng-Login.xml                 # Suite definitions
```

The rule is simple: **tests never touch the driver directly**. They interact with Screen objects, which use MobileActions, which use ElementHandler, which talks to the driver. This layered architecture is what makes the framework maintainable.

---

## 4. Setting Up the Foundation

### 4.1 Maven Project Setup

The `pom.xml` is the backbone. Key decisions:

**Java 17 compiler target:**
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

**Maven profiles for test suites:**

Each test suite gets its own Maven profile. This maps a `-P` flag to a TestNG XML file:

```xml
<profile>
    <id>Login</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>test-suite/testng-Login.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

Now `mvn test -P Login` runs exactly the login test suite. Clean, explicit, no guessing.

### 4.2 Configuration Management

Hardcoding device names, APK paths, and package names in code is a maintenance nightmare. Instead, everything lives in `config-stage.properties`:

```properties
platform=android
apkApp=/path/to/your/app.apk
deviceNameAndroid=Pixel_9_Pro_XL
androidPackage=com.example.yourapp
```

`SecretsHandler` reads this file using Java's `Properties` class:

```java
public static String getConfigValue(String key) {
    return getInstance().getProperty(key);
}
```

The file path is resolved based on the environment:
- **GitHub Actions**: reads from a system property (`-DCONFIG_PATH=...`)
- **Local staging**: reads from `src/main/java/Resources/config-stage.properties`
- **Local production**: reads from `config-prod.properties`

This means the same codebase works everywhere — CI, your machine, a colleague's machine — just by changing the config file.

### 4.3 Environment Detection

`CheckRunEnvironment` answers one question: *where is this code running?*

```java
public static boolean isRunningInGitHub() {
    return Boolean.parseBoolean(System.getenv("IS_GITHUB"));
}
```

This is used throughout the framework:
- `TestData` — decides whether to read config from system properties (CI) or files (local)
- `Log` — adjusts timestamps for IST timezone on GitHub Actions (which uses UTC)
- `TestListener` — only sends notifications when running in CI

---

## 5. Building the Core Engine

### 5.1 Appium Server Management

Most tutorials tell you to start Appium manually before running tests. That's fine for learning. It's unacceptable for CI/CD where there's no human to run `appium` in a terminal.

`AppiumServer` handles the full server lifecycle automatically:

**Starting a server:**
```java
AppiumServiceBuilder serviceBuilder = new AppiumServiceBuilder()
        .usingDriverExecutable(new File(nodePath))
        .withAppiumJS(new File(appiumPath))
        .usingPort(port)
        .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
        .withArgument(GeneralServerFlag.RELAXED_SECURITY)
        .withTimeout(Duration.ofSeconds(60));
```

**Port conflict resolution:**

If a previous test run crashed and left a server running, the framework detects it and cleans up:

```java
private static void stopExistingServersOnPort(int port) {
    // Check tracked instances
    if (serverInstances.containsKey(serverKey)) { ... }
    // Check system processes
    if (isAppiumServerRunningOnPort(port)) {
        forceStopProcessesOnPort(port);
    }
}
```

**Android SDK auto-detection:**

Instead of requiring `ANDROID_HOME` to be set, the server searches multiple locations:
1. `ANDROID_HOME` environment variable
2. `ANDROID_SDK_ROOT` environment variable
3. Common paths (`~/Library/Android/sdk`, `~/Android/Sdk`, etc.)
4. `which adb` command to find the SDK from the installed ADB

**Thread safety:**

`ThreadLocal<AppiumDriverLocalService>` gives each test thread its own server instance. A `ConcurrentHashMap` tracks all servers globally for cleanup.

### 5.2 Desired Capabilities

`DesiredCaps` configures how Appium interacts with the device. There are two distinct sets:

**Android (UiAutomator2):**
```java
capabilities.setCapability("automationName", "UiAutomator2");
capabilities.setCapability("autoGrantPermissions", true);
capabilities.setCapability("fullReset", true);
capabilities.setCapability("appium:adbExecTimeout", 60000);
```

Key choices:
- `fullReset: true` — reinstalls the app each session for a clean state
- `autoGrantPermissions: true` — no manual permission dialogs
- `adbExecTimeout: 60000` — generous timeout for slow emulators

**iOS (XCUITest):**
```java
capabilities.setCapability("automationName", "XCUITest");
capabilities.setCapability("noReset", true);
capabilities.setCapability("wdaStartupRetries", 4);
```

Key choices:
- `noReset: true` — iOS app installation is slow, so we preserve state
- `wdaStartupRetries: 4` — WebDriverAgent can be flaky, retries help

**Caching with ThreadLocal:**

Capabilities are created once per thread and cached:
```java
public static DesiredCapabilities getDesiredCapabilities(String platform) {
    DesiredCapabilities capabilities = capabilitiesThreadLocal.get();
    if (capabilities == null) {
        capabilities = createCapabilitiesForPlatform(platform);
        capabilitiesThreadLocal.set(capabilities);
    }
    return capabilities;
}
```

### 5.3 Driver Manager

`DriverManager` is the single entry point for all driver operations. Three methods are all you need:

```java
DriverManager.initializeDriver();          // Create driver for configured platform
AppiumDriver driver = DriverManager.getDriver();  // Get current thread's driver
DriverManager.quitDriver();                // Clean up everything
```

**How `initializeDriver()` works:**
1. Reads `platform` from `TestData.MobileCaps.PLATFORM`
2. Routes to `initializeAndroidDriver()` or `initializeIOSDriver()`
3. Gets capabilities from `DesiredCaps`
4. Starts Appium server if needed
5. Creates the platform-specific driver (`AndroidDriver` or `IOSDriver`)
6. Stores it in `ThreadLocal` for thread safety
7. Registers it with Selenide via `WebDriverRunner.setWebDriver()`
8. Configures Selenide timeouts

**How `quitDriver()` works:**
1. Quits the driver
2. Removes it from `ThreadLocal`
3. Clears `DesiredCaps` cache
4. Stops the Appium server

This comprehensive cleanup prevents resource leaks in long test runs.

---

## 6. Cross-Platform Element Handling

This is the heart of the framework's cross-platform support.

**The problem:** The same button might be:
- `//android.widget.Button[@content-desc='Submit']` on Android
- `//XCUIElementTypeButton[@name='Submit']` on iOS
- `submit-button` (accessibility ID) on both

**The solution:** `ElementHandler` provides a unified API:

```java
// Platform-specific locators
SelenideElement button = ElementHandler.getElement(
    "//android.widget.Button[@content-desc='Submit']",   // Android
    "//XCUIElementTypeButton[@name='Submit']"             // iOS
);

// Universal locator (same on both platforms)
SelenideElement button = ElementHandler.getElement("submit-button");

// Multiple elements
ElementsCollection images = ElementHandler.getElements(
    "//android.widget.ImageView",
    "//XCUIElementTypeImage"
);
```

**Auto-detection of locator type:**

The `handleLocator()` method inspects the string to determine the locator strategy:

```java
private static By handleLocator(String locatorPath) {
    if (locatorPath.startsWith("//") || locatorPath.startsWith("(//")) {
        return AppiumBy.xpath(locatorPath);          // XPath
    } else if (locatorPath.startsWith("android.") || locatorPath.startsWith("XCUIElementType")) {
        return AppiumBy.className(locatorPath);      // Class name
    } else {
        return AppiumBy.accessibilityId(locatorPath); // Accessibility ID
    }
}
```

This means you never need to specify `By.xpath()` or `AppiumBy.accessibilityId()` manually. Just pass the string and the framework figures it out.

**Platform detection** happens at runtime:
```java
if (WebDriverRunner.getWebDriver() instanceof AndroidDriver) {
    return $(handleLocator(androidLocator));
} else if (WebDriverRunner.getWebDriver() instanceof IOSDriver) {
    return $(handleLocator(iosLocator));
}
```

---

## 7. Mobile Interactions Layer

### 7.1 MobileActions

Every UI interaction goes through `MobileActions`. This centralizes logging, error handling, and platform-specific behavior.

**Basic interactions:**
```java
MobileActions.click(element, "Login Button");
MobileActions.inputText(element, "testuser", "Username Field");
MobileActions.clear(element, "Search Field");
String text = MobileActions.getText(element, "Header");
```

**Why `inputText` does click → clear → click → sendKeys:**

Mobile text fields are quirky. Sometimes `sendKeys` appends instead of replacing. Sometimes the field doesn't have focus. The sequence ensures:
1. Focus the field (first click)
2. Clear existing text
3. Re-focus (second click, because `clear()` can lose focus on some devices)
4. Type the new text

**Touch gestures using PointerInput API:**

Appium's `PointerInput` API gives precise control over touch gestures:

```java
// Tap
PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
Sequence tap = new Sequence(finger, 1);
tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.fromElement(element), 0, 0));
tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
driver.perform(Arrays.asList(tap));
```

**Smart scrolling with `scrollUntilVisible`:**

```java
MobileActions.scrollUntilVisible(driver, element, "Submit Button", Direction.DOWN);
```

This scrolls up to 10 times, checking after each scroll if the element is visible. It handles `StaleElementReferenceException` (the DOM changed during scroll) and `ElementNotFound` gracefully. If the element isn't found after 10 scrolls, it throws a clear error.

**Horizontal swiping:**

Calculates swipe coordinates based on the element's position and size:
```java
MobileActions.swipeElementHorizontal(driver, carousel, "Image Carousel", Direction.LEFT);
```

### 7.2 MobileVerifications

Simple boolean checks for element state:

```java
boolean isVisible = MobileVerifications.isElementVisible(element, "Button");
boolean isEnabled = MobileVerifications.isElementEnabled(element, "Button");
boolean isChecked = MobileVerifications.isElementChecked(element, "Checkbox");
```

These return `boolean` so you can use them in assertions or conditional logic.

### 7.3 NativeActions

Device-level actions that aren't UI element interactions:

```java
NativeActions.hideKeyboard(driver);              // Dismiss on-screen keyboard
NativeActions.pressBack(driver);                 // Android back button
NativeActions.pressHome(driver);                 // Android home button
NativeActions.pressDigits(driver, "1234");       // Type PIN via key events
NativeActions.runAppInBackground(driver, 5);     // Background for 5 seconds
NativeActions.terminateApp(driver);              // Kill the app
NativeActions.rotateLandscape(driver);           // Rotate screen
NativeActions.openNotifications(driver);         // Pull down notification shade
```

**Why `pressDigits` instead of `sendKeys` for PINs:**

PIN fields often have custom keyboards that don't respond to `sendKeys`. `pressDigits` simulates actual key presses:

```java
for (char c : digit.toCharArray()) {
    androidDriver.pressKey(new KeyEvent(AndroidKey.valueOf("DIGIT_" + c)));
}
```

### 7.4 WaitManager

Four wait strategies for different situations:

```java
// Fixed delay (use sparingly — only when no better option exists)
WaitManager.sleep(5);

// Implicit wait (global timeout for all element searches)
WaitManager.implicitWait(15);

// Explicit wait (wait for specific element to be visible)
WaitManager.explicitWait(element, "Login Button", 20);

// Fluent wait (custom polling interval, ignores specific exceptions)
WaitManager.fluentWait(element, "Dynamic Element", 30, 500);
```

**When to use which:**
- `implicitWait` — set once in `@BeforeClass`, applies to all `findElement` calls
- `explicitWait` — when you know a specific element takes longer to appear
- `fluentWait` — for elements that appear/disappear dynamically (like loading spinners)
- `sleep` — last resort, only when waiting for non-element conditions (animations, network calls)

---

## 8. Page Object Model — Screen Classes

Every app screen gets its own class. This is the **Page Object Model** pattern.

**Rules:**
1. Constructor accepts `AppiumDriver`
2. Elements are `private` fields using `ElementHandler.getElement()`
3. Public methods expose actions and verifications
4. Class name ends with `Screen`

**Example — LoginScreen:**

```java
public class LoginScreen {
    private AppiumDriver driver;

    public LoginScreen(AppiumDriver driver) {
        this.driver = driver;
    }

    // Elements (private — never accessed from tests directly)
    private SelenideElement phoneNumberField = ElementHandler.getElement(
        "//android.widget.EditText[@resource-id='phone-input']",
        "//XCUIElementTypeTextField[@name='phone-input']"
    );

    private SelenideElement submitButton = ElementHandler.getElement(
        "//android.widget.Button[@content-desc='Submit']",
        "//XCUIElementTypeButton[@name='Submit']"
    );

    // Actions (public — called from tests)
    public void inputPhoneNumber(String phoneNumber) {
        NativeActions.pressDigits(driver, phoneNumber);
    }

    public void clickSubmitButton() {
        MobileActions.click(submitButton, "Submit Button");
    }

    // Verifications (public — used in assertions)
    public boolean isPageVisible() {
        return MobileVerifications.isElementVisible(pageHeader, "Page Header");
    }

    // Flows (public — combine multiple actions)
    public void completeLoginScreenFlow(String phoneNumber) {
        inputPhoneNumber(phoneNumber);
        clickSubmitButton();
        WaitManager.implicitWait();
    }
}
```

**Why this matters:**

When the Submit button's locator changes from `content-desc='Submit'` to `content-desc='Log In'`, you update **one line** in `LoginScreen.java`. All 15 tests that click Submit continue to work.

---

## 9. Assertions with Auto-Screenshots

Standard TestNG assertions tell you *what* failed. They don't tell you *what the screen looked like* when it failed. The `Assertions` class fixes this.

```java
Assertions.assertEquals(actualTitle, "Login", "Verify page title");
Assertions.assertTrue(loginScreen.isPageVisible(), "Verify login screen is displayed");
```

**What happens on failure:**
1. Logs the failure with details
2. Captures a screenshot of the current screen
3. Attaches the screenshot to the Allure report
4. Throws an `AssertionError` with a descriptive message

**Screenshot capture:**
```java
private static void captureAndAttachScreenshot() {
    AppiumDriver driver = DriverManager.getDriver();
    if (driver instanceof TakesScreenshot) {
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        String threadId = Thread.currentThread().getName();
        Allure.addAttachment("screenshot_Thread_" + threadId, new ByteArrayInputStream(screenshot));
    }
}
```

The thread ID in the attachment name prevents conflicts during parallel execution.

**Available assertions:**
- `assertEquals` / `assertNotEquals` — object equality
- `assertTrue` / `assertFalse` — boolean conditions
- `assertNotNull` — also checks for zero values on Integer/Double/Float
- `assertEqualsList` — List<String> comparison
- `assertEqualsJson` — JSON comparison with lenient mode (ignores field order)
- `assertPass` / `assertFail` — explicit pass/fail markers

---

## 10. Test Structure and TestNG Integration

**Test class pattern:**

```java
@Epic("Sample App")
@Feature("Login")
@Story("Verify Login Screen")
@Severity(SeverityLevel.BLOCKER)
public class LoginScreenTest {

    protected static AppiumDriver driver;
    protected static LoginScreen loginScreen;

    @BeforeClass
    public void setUp() {
        DriverManager.initializeDriver();
        driver = DriverManager.getDriver();
        WaitManager.implicitWait();
        loginScreen = new LoginScreen(driver);
    }

    @Test(priority = 1)
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify Login Screen is displayed")
    public void Verify_Login_Screen_is_displayed() {
        Assertions.assertTrue(loginScreen.isPageVisible(), "Verify Login Screen is displayed");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
```

**Conventions:**
- `@BeforeClass` initializes driver and screen objects
- `@Test(priority = N)` controls execution order
- Test method names use underscores: `Verify_Something_Descriptive`
- Every test has `@Description` (displayed in Allure) and `@Severity`
- `@AfterClass(alwaysRun = true)` ensures cleanup even if tests fail
- Driver is `protected static` so it's shared across test methods

**TestNG Suite XML:**

```xml
<suite name="Login Scenarios">
    <listeners>
        <listener class-name="AppAutomation.Listener.TestListener"/>
    </listeners>
    <test name="Login Screen Test">
        <classes>
            <class name="AppAutomation.Login.LoginScreenTest"/>
        </classes>
    </test>
</suite>
```

The listener is registered here, not via annotations, so it applies to all tests in the suite.

---

## 11. Allure Reporting

Allure provides interactive HTML reports with:
- Test steps (from `Allure.step()` calls in assertions)
- Screenshots on failure (auto-captured by `Assertions`)
- Test descriptions (from `@Description` annotations)
- Severity levels and categorization (from `@Epic`, `@Feature`, `@Story`)

**AllureManager handles two things:**

1. **Test case naming** — uses `@Description` value instead of method name:
```java
public static void updateTestCases(ITestResult result) {
    getAllureLifecycle().updateTestCase(testResult ->
        testResult.setName(getTestDescription(result.getMethod().getConstructorOrMethod().getMethod())));
}
```

2. **Step wrapping** — for grouping actions in reports:
```java
AllureManager.executeStep("Navigate to login", () -> {
    pitchScreen.clickGetStartedButton();
});
```

**TestListener** hooks into the test lifecycle:
- `onTestStart` → updates Allure test case name
- `onTestSuccess/Failure/Skipped` → tracks counts
- `onStart(suite)` → clears previous Allure results (local only)
- `onFinish(suite)` → logs final summary

---

## 12. Logging

`Log` is a custom logger that writes to both console and file:

```java
Log.info("Starting test execution");     // Console + file
Log.error("Test failed: " + message);    // Console + file
Log.warn("Slow response detected");      // Console + file
Log.debug("Variable value: " + value);   // File only (no console clutter)
```

**Key design decisions:**
- **Singleton with double-checked locking** — thread-safe, one log file per run
- **Timestamp in filename** — `logs25-12-2023_14-30-45.txt` prevents overwrites
- **Dual output** — see results immediately in console, preserve everything in file
- **Debug goes to file only** — keeps console output clean during test runs
- **IST timezone adjustment for CI** — GitHub Actions runs in UTC, so timestamps are adjusted for readability

---

## 13. Running Tests

**Local execution:**

```bash
# Run a specific suite
mvn test -P Login

# Run a specific test class
mvn test -Dtest=LoginScreenTest

# Override platform
mvn test -P Login -Dplatform=ios
```

**Generate Allure report:**
```bash
mvn allure:serve    # Generates and opens in browser
mvn allure:report   # Generates without opening
```

**Prerequisites checklist:**
1. Java 17 installed (`java -version`)
2. Maven installed (`mvn -version`)
3. Appium installed (`appium --version`)
4. Appium driver installed (`appium driver list --installed`)
5. Android emulator running or device connected (`adb devices`)
6. `config-stage.properties` configured with your paths

---

## 14. Extending the Framework

### Adding a New Screen

1. Create `src/main/java/AppAutomation/Screens/Dashboard/DashboardScreen.java`:

```java
package AppAutomation.Screens.Dashboard;

import com.codeborne.selenide.SelenideElement;
import AppAutomation.Base.MobileActions;
import AppAutomation.Base.MobileVerifications;
import AppAutomation.Utils.ElementHandler;
import io.appium.java_client.AppiumDriver;

public class DashboardScreen {
    private AppiumDriver driver;

    public DashboardScreen(AppiumDriver driver) {
        this.driver = driver;
    }

    private SelenideElement welcomeText = ElementHandler.getElement(
        "//android.widget.TextView[@resource-id='welcome-text']",
        "//XCUIElementTypeStaticText[@name='welcome-text']"
    );

    public boolean isPageVisible() {
        return MobileVerifications.isElementVisible(welcomeText, "Welcome Text");
    }

    public String getWelcomeText() {
        return MobileActions.getText(welcomeText, "Welcome Text");
    }
}
```

### Adding a New Test Class

2. Create `src/test/java/AppAutomation/Dashboard/DashboardScreenTest.java`:

```java
package AppAutomation.Dashboard;

import org.testng.annotations.*;
import AppAutomation.Assertions.Assertions;
import AppAutomation.Base.DriverManager;
import AppAutomation.Screens.Dashboard.DashboardScreen;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.*;

@Epic("Sample App")
@Feature("Dashboard")
public class DashboardScreenTest {

    protected static AppiumDriver driver;
    protected static DashboardScreen dashboardScreen;

    @BeforeClass
    public void setUp() {
        DriverManager.initializeDriver();
        driver = DriverManager.getDriver();
        // Navigate to dashboard...
        dashboardScreen = new DashboardScreen(driver);
    }

    @Test(priority = 1)
    @Description("Verify Dashboard is displayed")
    public void Verify_Dashboard_is_displayed() {
        Assertions.assertTrue(dashboardScreen.isPageVisible(), "Dashboard is displayed");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
```

### Adding a New Test Suite

3. Create `test-suite/testng-Dashboard.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "Https://testng.org/testng-1.0.dtd">
<suite name="Dashboard Scenarios">
    <listeners>
        <listener class-name="AppAutomation.Listener.TestListener"/>
    </listeners>
    <test name="Dashboard Screen Test">
        <classes>
            <class name="AppAutomation.Dashboard.DashboardScreenTest"/>
        </classes>
    </test>
</suite>
```

4. Add Maven profile in `pom.xml`:

```xml
<profile>
    <id>Dashboard</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.19.1</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>test-suite/testng-Dashboard.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>
            <plugin>
                <groupId>io.qameta.allure</groupId>
                <artifactId>allure-maven</artifactId>
                <version>2.12.0</version>
            </plugin>
        </plugins>
    </build>
</profile>
```

Now run: `mvn test -P Dashboard`

---

## 15. CI/CD Integration

The framework is designed to run on GitHub Actions with zero manual intervention.

**How it works in CI:**

1. Set environment variable `IS_GITHUB=true`
2. Pass config values as system properties:
```bash
mvn test -P Login \
  -Dplatform=android \
  -DapkApp=/path/to/app.apk \
  -DdeviceName=emulator-5554 \
  -DandroidPackage=com.example.app
```

3. The framework automatically:
   - Detects GitHub Actions via `CheckRunEnvironment.isRunningInGitHub()`
   - Reads config from system properties instead of files
   - Starts and manages the Appium server
   - Adjusts log timestamps for IST timezone
   - Generates Allure report artifacts

**Sample GitHub Actions workflow:**

```yaml
name: Mobile Tests
on: [push]
jobs:
  test:
    runs-on: macos-latest
    env:
      IS_GITHUB: true
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Install Appium
        run: npm install -g appium && appium driver install uiautomator2
      - name: Start Emulator
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          script: mvn test -P Login -Dplatform=android -DapkApp=app.apk -DdeviceName=emulator-5554 -DandroidPackage=com.example.app
      - name: Allure Report
        uses: simple-elf/allure-report-action@v1
        if: always()
        with:
          allure_results: allure-results
```

---

## Summary

This framework provides a clean separation of concerns:

| Layer | Responsibility | Changes When... |
|-------|---------------|-----------------|
| **Config** | Where to find the app, which device | Environment changes |
| **Base** | How to start drivers, interact with elements | Appium API changes |
| **ElementHandler** | How to find elements cross-platform | Locator strategy changes |
| **Screens** | What elements exist on each screen | UI changes |
| **Tests** | What to verify | Requirements change |

Each layer shields the ones above it from change. A new Appium version? Update `Base`. A redesigned login screen? Update `LoginScreen`. A new test requirement? Add a `@Test` method. Nothing else needs to change.

That's the whole point of a framework — **making the next 100 tests as easy to write as the first one**.
