package AppAutomation.Assertions;

import java.util.List;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.Assert;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import java.io.ByteArrayInputStream;

import AppAutomation.Base.DriverManager;
import AppAutomation.Utils.Log;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;

public class Assertions {

	public static void assertEquals(Object actualValue, Object expectedValue, String testCase) {
		Log.info("Assertion: " + testCase);
		try {
			Assert.assertEquals(actualValue, expectedValue);
			Allure.step(testCase);
			Log.info("Assertion passed: " + testCase);
		} catch (AssertionError e) {
			Log.error("Assertion failed: " + testCase);
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertEqualsList(List<String> actualValue, List<String> expectedValue, String testCase) {
		Log.info("List assertion: " + testCase);
		try {
			Assert.assertEquals(actualValue, expectedValue);
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertEqualsListObject(List<Object> actualValue, List<Object> expectedValue, String testCase) {
		Log.info("Object list assertion: " + testCase);
		try {
			Assert.assertEquals(actualValue, expectedValue);
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertEqualsJson(String actualValue, String expectedValue, String testCase) {
		Log.info("JSON assertion: " + testCase);
		try {
			JSONAssert.assertEquals(actualValue, expectedValue, JSONCompareMode.LENIENT);
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertNotEquals(Object actualValue, Object expectedValue, String testCase) {
		Log.info("Inequality assertion: " + testCase);
		try {
			Assert.assertNotEquals(actualValue, expectedValue);
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertTrue(boolean expectedValue, String testCase) {
		Log.info("True assertion: " + testCase);
		try {
			Assert.assertTrue(expectedValue);
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertFalse(boolean expectedValue, String testCase) {
		Log.info("False assertion: " + testCase);
		try {
			Assert.assertFalse(expectedValue);
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertNotNull(Object value, String testCase) {
		Log.info("Not null assertion: " + testCase);
		try {
			Assert.assertNotNull(value);
			if (value instanceof Integer && (Integer) value == 0) {
				throw new AssertionError("Expected Integer not to be 0");
			} else if (value instanceof Double && (Double) value == 0.0) {
				throw new AssertionError("Expected Double not to be 0");
			} else if (value instanceof Float && (Float) value == 0.0) {
				throw new AssertionError("Expected Float not to be 0");
			}
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertNull(Object value, String testCase) {
		Log.info("Null assertion: " + testCase);
		try {
			Assert.assertNull(value);
			Allure.step(testCase);
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertFail(String testCase) {
		Log.info("Explicit failure: " + testCase);
		try {
			Assert.fail();
		} catch (AssertionError e) {
			captureAndAttachScreenshot();
			throw new AssertionError("Failure: " + testCase + System.lineSeparator() + e);
		}
	}

	public static void assertPass(String testCase) {
		Log.info("Explicit pass: " + testCase);
		Allure.step(testCase);
	}

	private static void captureAndAttachScreenshot() {
		try {
			AppiumDriver driver = DriverManager.getDriver();
			if (driver == null) return;

			if (driver instanceof TakesScreenshot) {
				byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
				String threadId = Thread.currentThread().getName();
				Allure.addAttachment("screenshot_Thread_" + threadId, new ByteArrayInputStream(screenshot));
			}
		} catch (Exception e) {
			Log.error("Failed to capture screenshot: " + e.getMessage());
		}
	}
}
