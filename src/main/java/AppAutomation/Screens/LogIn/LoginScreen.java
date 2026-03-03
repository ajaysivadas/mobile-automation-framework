package AppAutomation.Screens.LogIn;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import AppAutomation.Base.MobileActions;
import AppAutomation.Base.MobileVerifications;
import AppAutomation.Base.NativeActions;
import AppAutomation.Base.WaitManager;
import AppAutomation.Utils.ElementHandler;
import AppAutomation.Utils.Log;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;

public class LoginScreen {
    private AppiumDriver driver;

    public LoginScreen(AppiumDriver driver) {
        this.driver = driver;
    }

    // TODO: Update these locators to match your app's login screen
    private SelenideElement phoneNumberField = ElementHandler.getElement("//android.widget.EditText[@resource-id='phone-input']", "//XCUIElementTypeTextField[@name='phone-input']");

    private SelenideElement submitButton = ElementHandler.getElement("//android.widget.Button[@content-desc='Submit']", "//XCUIElementTypeButton[@name='Submit']");

    private SelenideElement pageHeader = ElementHandler.getElement("//android.view.View[contains(@content-desc,'Login')]", "//XCUIElementTypeStaticText[contains(@name,'Login')]");

    private SelenideElement phoneNumberFieldHeader = ElementHandler.getElement("PHONE NUMBER");

    private ElementsCollection allImages = ElementHandler.getElements("//android.widget.ImageView", "//XCUIElementTypeImage");

    // Screen visibility
    public boolean isPageVisible() {
        return MobileVerifications.isElementVisible(pageHeader, "Page Header");
    }

    // Inputs
    public void inputPhoneNumber(String phoneNumber) {
        NativeActions.pressDigits(driver, phoneNumber);
    }

    // Buttons
    public void clickSubmitButton() {
        MobileActions.click(submitButton, "Submit Button");
    }

    // Button visibility
    public boolean isSubmitButtonVisible() {
        return MobileVerifications.isElementVisible(submitButton, "Submit Button");
    }

    // Texts
    public String getPageHeader() {
        return MobileActions.getContentDescription(pageHeader, "Page Header");
    }

    public String getPhoneNumberFieldHeader() {
        return MobileActions.getContentDescription(phoneNumberFieldHeader, "Phone Number Field Header");
    }

    // Images
    public int getImagesCount() {
        return allImages.size();
    }

    // Flow
    public void completeLoginScreenFlow(String phoneNumber) {
        inputPhoneNumber(phoneNumber);
        clickSubmitButton();
        WaitManager.implicitWait();
    }
}
