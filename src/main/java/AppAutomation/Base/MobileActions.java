package AppAutomation.Base;

import java.time.Duration;
import java.util.Arrays;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.ElementNotFound;

import AppAutomation.Utils.Log;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class MobileActions {

    public static void click(SelenideElement element, String elementName) {
        Log.info("Clicking on " + elementName);
        element.shouldBe(Condition.enabled).click();
        Log.info("Clicked on " + elementName);
    }

    public static void inputText(SelenideElement element, String text, String elementName) {
        Log.info("Sending keys to " + elementName);
        element.shouldBe(Condition.visible);
        element.click();
        element.clear();
        element.click();
        element.sendKeys(text);
        Log.info("Sent keys to " + elementName);
    }

    public static void clear(SelenideElement element, String elementName) {
        Log.info("Clearing " + elementName);
        element.shouldBe(Condition.visible).click();
        element.clear();
        Log.info("Cleared " + elementName);
    }

    public static String getText(SelenideElement element, String elementName) {
        Log.info("Getting text from " + elementName);
        String text = element.shouldBe(Condition.visible).getText();
        Log.info("Got text '" + text + "' from " + elementName);
        return text;
    }

    public static String getAttributeValue(SelenideElement element, String androidAttributeName, String iosAttributeName, String elementName) {
        if (WebDriverRunner.getWebDriver() instanceof AndroidDriver) {
            String text = element.shouldBe(Condition.visible).getAttribute(androidAttributeName).trim();
            Log.info("Got attribute " + androidAttributeName + " = '" + text + "' from " + elementName);
            return text;
        } else if (WebDriverRunner.getWebDriver() instanceof IOSDriver) {
            String text = element.shouldBe(Condition.visible).getAttribute(iosAttributeName).trim();
            Log.info("Got attribute " + iosAttributeName + " = '" + text + "' from " + elementName);
            return text;
        } else {
            throw new IllegalArgumentException("WebDriver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    public static String getContentDescription(SelenideElement element, String elementName) {
        Log.info("Getting content description from " + elementName);
        if (WebDriverRunner.getWebDriver() instanceof AndroidDriver) {
            String text = element.shouldBe(Condition.visible).getAttribute("content-desc").replaceAll("\n", " ");
            Log.info("Got content description: '" + text + "' from " + elementName);
            return text;
        } else if (WebDriverRunner.getWebDriver() instanceof IOSDriver) {
            String text = element.shouldBe(Condition.visible).getAttribute("name").replaceAll("\n", " ");
            Log.info("Got content description: '" + text + "' from " + elementName);
            return text;
        } else {
            throw new IllegalArgumentException("WebDriver is not an instance of AndroidDriver or IOSDriver");
        }
    }

    public static void selectFromDropdown(SelenideElement element, String text, String elementName) {
        Log.info("Selecting '" + text + "' from dropdown " + elementName);
        element.shouldBe(Condition.visible).selectOption(text);
        Log.info("Selected from dropdown " + elementName);
    }

    public static void selectFromDropdownByIndex(SelenideElement element, int index, String elementName) {
        Log.info("Selecting index " + index + " from dropdown " + elementName);
        element.shouldBe(Condition.visible).selectOption(index);
        Log.info("Selected from dropdown " + elementName);
    }

    public static void toggleRadioButton(SelenideElement element, ToggleState desiredState, String elementName) {
        Log.info("Toggling radio button " + elementName + " to " + desiredState);
        element.shouldBe(Condition.visible);
        boolean isCurrentlyOn = MobileVerifications.isElementChecked(element, elementName);
        boolean shouldBeOn = (desiredState == ToggleState.ENABLED);
        if (isCurrentlyOn != shouldBeOn) {
            element.click();
        }
    }

    public static void toggleCheckbox(SelenideElement element, ToggleState desiredState, String elementName) {
        Log.info("Toggling checkbox " + elementName + " to " + desiredState);
        element.shouldBe(Condition.visible);
        boolean isCurrentlyChecked = element.is(Condition.selected);
        boolean shouldBeChecked = (desiredState == ToggleState.ENABLED);
        if (isCurrentlyChecked != shouldBeChecked) {
            element.click();
        }
    }

    public static void tap(AppiumDriver driver, SelenideElement element, String elementName) {
        element.shouldBe(Condition.visible);
        Log.info("Tapping on " + elementName);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.fromElement(element), 0, 0));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(tap));
        Log.info("Tapped on " + elementName);
    }

    public static void longPress(AppiumDriver driver, SelenideElement element, String elementName) {
        Log.info("Long pressing on " + elementName);
        element.shouldBe(Condition.visible);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence longPress = new Sequence(finger, 1);
        longPress.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.fromElement(element), 0, 0));
        longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        longPress.addAction(new org.openqa.selenium.interactions.Pause(finger, Duration.ofSeconds(3)));
        longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(longPress));
        Log.info("Long pressed on " + elementName);
    }

    public static SelenideElement scrollUntilVisible(AppiumDriver driver, SelenideElement element, String elementName, Direction direction) {
        Log.info("Scrolling " + direction + " until " + elementName + " is visible");
        int maxScrolls = 10;
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY, endY;

        if (direction == Direction.UP) {
            startY = (int) (size.height * 0.2);
            endY   = (int) (size.height * 0.8);
        } else if (direction == Direction.DOWN) {
            startY = (int) (size.height * 0.8);
            endY   = (int) (size.height * 0.2);
        } else {
            throw new IllegalArgumentException("Direction must be UP or DOWN");
        }

        for (int i = 0; i < maxScrolls; i++) {
            try {
                if (element.is(Condition.visible)) {
                    Log.info("Element " + elementName + " is visible");
                    return element;
                }
            } catch (StaleElementReferenceException | ElementNotFound | NoSuchElementException ignored) {}

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(), startX, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Arrays.asList(swipe));
            Log.info("Scroll attempt " + (i + 1) + " for " + elementName);
        }

        throw new NoSuchElementException("Element not found after scrolling: " + elementName);
    }

    public static void swipeElementHorizontal(AppiumDriver driver, SelenideElement element, String elementName, Direction direction) {
        Log.info("Swiping " + elementName + " in " + direction + " direction");
        element.shouldBe(Condition.visible);
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int startY = location.getY() + size.getHeight() / 2;
        int startX, endX;

        if (direction == Direction.LEFT) {
            startX = location.getX() + (int) (size.getWidth() * 0.9);
            endX   = location.getX() + (int) (size.getWidth() * 0.1);
        } else if (direction == Direction.RIGHT) {
            startX = location.getX() + (int) (size.getWidth() * 0.1);
            endX   = location.getX() + (int) (size.getWidth() * 0.9);
        } else {
            throw new IllegalArgumentException("Direction must be LEFT or RIGHT");
        }

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(), endX, startY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
        Log.info("Swiped " + elementName + " in " + direction + " direction");
    }

    public static enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    public enum ToggleState {
        ENABLED, DISABLED
    }
}
