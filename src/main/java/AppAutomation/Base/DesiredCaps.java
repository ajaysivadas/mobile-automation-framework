package AppAutomation.Base;

import io.appium.java_client.remote.MobilePlatform;
import org.openqa.selenium.remote.DesiredCapabilities;

import AppAutomation.TestData.TestData;
import AppAutomation.Utils.Log;

public class DesiredCaps {

    private static final ThreadLocal<DesiredCapabilities> capabilitiesThreadLocal = new ThreadLocal<>();

    public static DesiredCapabilities getDesiredCapabilities(String platform) {
        DesiredCapabilities capabilities = capabilitiesThreadLocal.get();
        if (capabilities == null) {
            capabilities = createCapabilitiesForPlatform(platform);
            capabilitiesThreadLocal.set(capabilities);
        }
        return capabilities;
    }

    private static DesiredCapabilities getAndroidCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", MobilePlatform.ANDROID);
        capabilities.setCapability("deviceName", TestData.MobileCaps.DEVICE_NAME_ANDROID);
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("app", TestData.MobileCaps.APP_APK);
        capabilities.setCapability("appPackage", TestData.MobileCaps.ANDROID_PACKAGE);

        capabilities.setCapability("newCommandTimeout", 300);
        capabilities.setCapability("noReset", false);
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability("autoAcceptAlerts", true);
        capabilities.setCapability("forceAppLaunch", true);

        capabilities.setCapability("appium:adbExecTimeout", 60000);
        capabilities.setCapability("appium:uiautomator2ServerInstallTimeout", 60000);
        capabilities.setCapability("appium:uiautomator2ServerLaunchTimeout", 60000);
        capabilities.setCapability("appium:skipServerInstallation", false);
        capabilities.setCapability("appium:systemPort", 8201);
        capabilities.setCapability("appium:androidInstallTimeout", 90000);
        capabilities.setCapability("appium:androidDeviceReadyTimeout", 60);

        Log.info("Android capabilities configured - Device: " + TestData.MobileCaps.DEVICE_NAME_ANDROID);
        return capabilities;
    }

    private static DesiredCapabilities getIOSCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", MobilePlatform.IOS);
        capabilities.setCapability("deviceName", TestData.MobileCaps.DEVICE_NAME_IOS);
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("app", TestData.MobileCaps.APP_IPA);
        capabilities.setCapability("bundleId", TestData.MobileCaps.IOS_BUNDLE_ID);

        capabilities.setCapability("newCommandTimeout", 300);
        capabilities.setCapability("noReset", true);
        capabilities.setCapability("autoAcceptAlerts", true);
        capabilities.setCapability("wdaStartupRetries", 4);
        capabilities.setCapability("forceAppLaunch", true);

        Log.info("iOS capabilities configured - Device: " + TestData.MobileCaps.DEVICE_NAME_IOS);
        return capabilities;
    }

    private static DesiredCapabilities createCapabilitiesForPlatform(String platform) {
        if ("ios".equals(platform)) {
            return getIOSCapabilities();
        } else {
            return getAndroidCapabilities();
        }
    }

    public static void clearThreadLocalStorage() {
        capabilitiesThreadLocal.remove();
    }
}
