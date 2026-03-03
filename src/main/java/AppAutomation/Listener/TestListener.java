package AppAutomation.Listener;

import java.io.File;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import AppAutomation.Utils.AllureManager;
import AppAutomation.Utils.CheckRunEnvironment;
import AppAutomation.Utils.Log;

public class TestListener implements ITestListener, ISuiteListener {

	private static int passedTests = 0;
	private static int failedTests = 0;
	private static int skippedTests = 0;

	@Override
	public void onTestStart(ITestResult result) {
		AllureManager.updateTestCases(result);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		passedTests++;
	}

	@Override
	public void onStart(ISuite suite) {
		if (!CheckRunEnvironment.isRunningInGitHub()) {
			String resultsDirectoryPath = System.getProperty("user.dir") + "/allure-results";
			File resultsDirectory = new File(resultsDirectoryPath);
			if (resultsDirectory.exists()) {
				deleteDirectory(resultsDirectory);
				Log.info("Allure results cleared successfully.");
			}
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		failedTests++;
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		skippedTests++;
	}

	@Override
	public void onFinish(ISuite suite) {
		Log.info("Suite finished - Passed: " + passedTests + ", Failed: " + failedTests + ", Skipped: " + skippedTests);
	}

	private void deleteDirectory(File directory) {
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				}
				file.delete();
			}
		}
		directory.delete();
	}
}
