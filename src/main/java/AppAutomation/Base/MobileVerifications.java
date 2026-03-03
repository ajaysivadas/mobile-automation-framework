package AppAutomation.Base;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import AppAutomation.Utils.Log;

public class MobileVerifications {

    public static boolean isElementVisible(SelenideElement element, String elementName) {
        Log.info("Verifying element " + elementName + " is visible");
        return element.is(Condition.visible);
    }

    public static boolean isElementEnabled(SelenideElement element, String elementName) {
        Log.info("Verifying element " + elementName + " is enabled");
        return element.is(Condition.enabled);
    }

    public static boolean isElementClickable(SelenideElement element, String elementName) {
        Log.info("Verifying element " + elementName + " is clickable");
        return element.is(Condition.clickable);
    }

    public static boolean isElementSelected(SelenideElement element, String elementName) {
        Log.info("Verifying element " + elementName + " is selected");
        return element.is(Condition.selected);
    }

    public static boolean isElementChecked(SelenideElement element, String elementName) {
        Log.info("Verifying element " + elementName + " is checked");
        return Boolean.parseBoolean(MobileActions.getAttributeValue(element, "checked", "checked", elementName));
    }

    public static boolean isImageLoaded(SelenideElement element, String elementName) {
        Log.info("Verifying element " + elementName + " is loaded");
        return element.is(Condition.image);
    }
}
