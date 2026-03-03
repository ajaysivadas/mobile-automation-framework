package AppAutomation.Login;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import AppAutomation.Assertions.Assertions;
import AppAutomation.Base.DriverManager;
import AppAutomation.Base.WaitManager;
import AppAutomation.Screens.LogIn.LoginScreen;
import AppAutomation.TestData.TestData;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

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

    @Test(priority = 2)
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify Submit Button is visible after entering phone number")
    public void Verify_Submit_Button_is_visible_after_entering_phone_number() {
        loginScreen.inputPhoneNumber(TestData.AppUserData.USER_PHONE_NUMBER);
        Assertions.assertTrue(loginScreen.isSubmitButtonVisible(), "Verify Submit Button is visible");
    }

    @Test(priority = 3)
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify Page Header Text is correct")
    public void Verify_Page_Header_Text_is_correct() {
        // TODO: Update expected text to match your app
        Assertions.assertEquals(loginScreen.getPageHeader(), "Login", "Verify Page Header Text is correct");
    }

    @Test(priority = 4)
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify Phone Number Field Header is correct")
    public void Verify_Phone_Number_Field_Header_is_correct() {
        Assertions.assertEquals(loginScreen.getPhoneNumberFieldHeader(), "PHONE NUMBER", "Verify Phone Number Field Header is correct");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
