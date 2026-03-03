package AppAutomation.Base;

import java.time.Duration;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.FluentWait;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import AppAutomation.Utils.Log;

public class WaitManager {

    private static final long DEFAULT_TIMEOUT = 10;
    private static final long DEFAULT_POLLING_MILLIS = 100;

    public static void sleep(int seconds) {
        Log.info("Sleeping for " + seconds + " seconds");
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep() {
        sleep((int) DEFAULT_TIMEOUT);
    }

    public static void implicitWait(long seconds) {
        Log.info("Setting implicit wait to " + seconds + " seconds");
        WebDriverRunner.getWebDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
    }

    public static void implicitWait() {
        implicitWait(DEFAULT_TIMEOUT);
    }

    public static void explicitWait(SelenideElement element, String elementName, long seconds) {
        Log.info("Waiting for " + elementName + " to be visible for " + seconds + " seconds");
        element.shouldBe(Condition.visible, Duration.ofSeconds(seconds));
    }

    public static void explicitWait(SelenideElement element, String elementName) {
        explicitWait(element, elementName, DEFAULT_TIMEOUT);
    }

    public static void fluentWait(SelenideElement element, String elementName, long timeoutSeconds, long pollingMillis) {
        Log.info("Fluent wait for " + elementName + " - timeout: " + timeoutSeconds + "s, polling: " + pollingMillis + "ms");
        new FluentWait<>(element)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(pollingMillis))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .until(SelenideElement::exists);
    }

    public static void fluentWait(SelenideElement element, String elementName) {
        fluentWait(element, elementName, DEFAULT_TIMEOUT, DEFAULT_POLLING_MILLIS);
    }
}
