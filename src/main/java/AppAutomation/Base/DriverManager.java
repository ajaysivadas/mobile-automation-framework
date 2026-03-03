package AppAutomation.Base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;

import AppAutomation.TestData.TestData;
import AppAutomation.Utils.Log;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class DriverManager {

    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();
    private static final int DEFAULT_IMPLICIT_WAIT = 30;

    public static void initializeAndroidDriver() {
        try {
            Log.info("Starting Android driver initialization for thread: " + Thread.currentThread().getName());

            DesiredCapabilities capabilities = DesiredCaps.getDesiredCapabilities("android");

            AppiumDriverLocalService server = AppiumServer.getCurrentServer();
            if (server == null || !server.isRunning()) {
                server = AppiumServer.startServer();
            }

            AppiumDriver driver = new AndroidDriver(server.getUrl(), capabilities);
            driverThreadLocal.set(driver);
            WebDriverRunner.setWebDriver(driver);

            configureSelenideForAppium();
            Log.info("Android driver initialized successfully on thread: " + Thread.currentThread().getName());

        } catch (Exception e) {
            Log.error("Failed to initialize Android driver: " + e.getMessage());
            throw new RuntimeException("Android driver initialization failed", e);
        }
    }

    public static void initializeIOSDriver() {
        try {
            Log.info("Starting iOS driver initialization for thread: " + Thread.currentThread().getName());

            DesiredCapabilities capabilities = DesiredCaps.getDesiredCapabilities("ios");

            AppiumDriverLocalService server = AppiumServer.getCurrentServer();
            if (server == null || !server.isRunning()) {
                server = AppiumServer.startServer();
            }

            AppiumDriver driver = new IOSDriver(server.getUrl(), capabilities);
            driverThreadLocal.set(driver);
            WebDriverRunner.setWebDriver(driver);

            configureSelenideForAppium();
            Log.info("iOS driver initialized successfully on thread: " + Thread.currentThread().getName());

        } catch (Exception e) {
            Log.error("Failed to initialize iOS driver: " + e.getMessage());
            throw new RuntimeException("iOS driver initialization failed", e);
        }
    }

    public static void initializeDriver() {
        String platform = TestData.MobileCaps.PLATFORM;
        Log.info("Initializing driver for platform: " + platform);

        if ("android".equalsIgnoreCase(platform)) {
            initializeAndroidDriver();
        } else if ("ios".equalsIgnoreCase(platform)) {
            initializeIOSDriver();
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platform + ". Supported: android, ios");
        }
    }

    public static AppiumDriver getDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized for thread: " + Thread.currentThread().getName() + ". Call initializeDriver() first.");
        }
        return driver;
    }

    public static void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                Log.info("Driver quit successfully for thread: " + Thread.currentThread().getName());
            } catch (Exception e) {
                Log.error("Error while quitting driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
                DesiredCaps.clearThreadLocalStorage();
                AppiumServer.stopServer();
            }
        }
    }

    private static void configureSelenideForAppium() {
        Configuration.timeout = DEFAULT_IMPLICIT_WAIT * 1000;
        Configuration.browser = "chrome";
    }

    public static void resetThreadLocalStorage() {
        driverThreadLocal.remove();
        DesiredCaps.clearThreadLocalStorage();
        AppiumServer.stopServer();
    }
}
