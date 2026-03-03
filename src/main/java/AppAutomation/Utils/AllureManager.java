package AppAutomation.Utils;

import java.lang.reflect.Method;

import org.testng.ITestResult;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.Description;

public class AllureManager {

	public static void updateTestCases(ITestResult result) {
		getAllureLifecycle().updateTestCase(testResult -> testResult.setName(getTestDescription(result.getMethod().getConstructorOrMethod().getMethod())));
	}

	public static void executeStep(String stepName, Runnable methodToExecute) {
		Allure.step(stepName, methodToExecute::run);
	}

	private static AllureLifecycle getAllureLifecycle() {
		return Allure.getLifecycle();
	}

	private static String getTestDescription(Method method) {
		Description description = method.getAnnotation(Description.class);
		return description != null ? description.value() : "No description available";
	}
}
