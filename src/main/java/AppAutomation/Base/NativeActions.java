package AppAutomation.Base;

import java.time.Duration;

import org.openqa.selenium.ScreenOrientation;

import AppAutomation.TestData.TestData;
import AppAutomation.Utils.Log;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.ios.IOSDriver;

public class NativeActions {

    public static void hideKeyboard(AppiumDriver driver) {
        if (driver instanceof AndroidDriver) {
            if (((AndroidDriver) driver).isKeyboardShown()) {
                ((AndroidDriver) driver).hideKeyboard();
            }
        } else if (driver instanceof IOSDriver) {
            if (((IOSDriver) driver).isKeyboardShown()) {
                ((IOSDriver) driver).hideKeyboard();
            }
        } else {
            throw new IllegalArgumentException("Driver is not an instance of AndroidDriver or IOSDriver");
        }
        Log.info("Keyboard hidden successfully");
    }

    public static void runAppInBackground(AppiumDriver driver, int seconds) {
        Log.info("Running app in background for " + seconds + " seconds");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).runAppInBackground(Duration.ofSeconds(seconds));
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).runAppInBackground(Duration.ofSeconds(seconds));
        } else {
            throw new IllegalArgumentException("Driver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    public static void terminateApp(AppiumDriver driver) {
        Log.info("Terminating application");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).terminateApp(TestData.MobileCaps.ANDROID_PACKAGE);
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).terminateApp(TestData.MobileCaps.IOS_BUNDLE_ID);
        } else {
            throw new IllegalArgumentException("Driver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    public static void pressBack(AppiumDriver driver) {
        Log.info("Pressing back button");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        } else {
            throw new IllegalArgumentException("pressBack is only supported on Android");
        }
    }

    public static void pressHome(AppiumDriver driver) {
        Log.info("Pressing home button");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.HOME));
        } else {
            throw new IllegalArgumentException("pressHome is only supported on Android");
        }
    }

    public static void pressDigits(AppiumDriver driver, String digit) {
        Log.info("Pressing digits: " + digit);
        if (driver instanceof AndroidDriver) {
            AndroidDriver androidDriver = (AndroidDriver) driver;
            for (char c : digit.toCharArray()) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.valueOf("DIGIT_" + c)));
            }
        } else {
            throw new IllegalArgumentException("pressDigits is only supported on Android");
        }
    }

    public static void openAppSwitch(AppiumDriver driver) {
        Log.info("Opening app switcher");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.APP_SWITCH));
        } else {
            throw new IllegalArgumentException("openAppSwitch is only supported on Android");
        }
    }

    public static void openNotifications(AppiumDriver driver) {
        Log.info("Opening notifications panel");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).openNotifications();
        } else {
            throw new IllegalArgumentException("openNotifications is only supported on Android");
        }
    }

    public static void rotateLandscape(AppiumDriver driver) {
        Log.info("Rotating device to landscape");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).rotate(ScreenOrientation.LANDSCAPE);
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).rotate(ScreenOrientation.LANDSCAPE);
        } else {
            throw new IllegalArgumentException("Driver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    public static void rotatePortrait(AppiumDriver driver) {
        Log.info("Rotating device to portrait");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).rotate(ScreenOrientation.PORTRAIT);
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).rotate(ScreenOrientation.PORTRAIT);
        } else {
            throw new IllegalArgumentException("Driver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    public enum DeviceToggleState {
        LOCK, UNLOCK
    }
}
