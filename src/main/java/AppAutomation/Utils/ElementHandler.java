package AppAutomation.Utils;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ElementHandler {

    public static SelenideElement getElement(String androidLocator, String iosLocator) {
        if (WebDriverRunner.getWebDriver() instanceof AndroidDriver) {
            return $(handleLocator(androidLocator));
        } else if (WebDriverRunner.getWebDriver() instanceof IOSDriver) {
            return $(handleLocator(iosLocator));
        } else {
            throw new IllegalArgumentException("WebDriver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    public static SelenideElement getElement(String anyLocator) {
        return $(handleLocator(anyLocator));
    }

    public static ElementsCollection getElements(String androidLocator, String iosLocator) {
        if (WebDriverRunner.getWebDriver() instanceof AndroidDriver) {
            return $$(handleLocator(androidLocator));
        } else if (WebDriverRunner.getWebDriver() instanceof IOSDriver) {
            return $$(handleLocator(iosLocator));
        } else {
            throw new IllegalArgumentException("WebDriver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    private static By handleLocator(String locatorPath) {
        if (locatorPath.startsWith("//") || locatorPath.startsWith("(//")) {
            return AppiumBy.xpath(locatorPath);
        } else if (locatorPath.startsWith("android.") || locatorPath.startsWith("XCUIElementType")) {
            return AppiumBy.className(locatorPath);
        } else {
            return AppiumBy.accessibilityId(locatorPath);
        }
    }
}
