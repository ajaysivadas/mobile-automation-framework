package AppAutomation.TestData;

import AppAutomation.Utils.CheckRunEnvironment;
import AppAutomation.Utils.SecretsHandler;

public class TestData {

	public static class MobileCaps {

		public static final String PLATFORM = CheckRunEnvironment.isRunningInGitHub() ? System.getProperty("platform").trim() : SecretsHandler.getConfigValue("platform").trim();
		public static final String APP_APK = CheckRunEnvironment.isRunningInGitHub() ? System.getProperty("apkApp").trim() : SecretsHandler.getConfigValue("apkApp").trim();
		public static final String APP_IPA = CheckRunEnvironment.isRunningInGitHub() ? System.getProperty("ipaApp").trim() : SecretsHandler.getConfigValue("ipaApp").trim();
		public static final String DEVICE_NAME_ANDROID = CheckRunEnvironment.isRunningInGitHub() ? System.getProperty("deviceName").trim() : SecretsHandler.getConfigValue("deviceNameAndroid").trim();
		public static final String DEVICE_NAME_IOS = CheckRunEnvironment.isRunningInGitHub() ? System.getProperty("deviceName").trim() : SecretsHandler.getConfigValue("deviceNameIOS").trim();
		public static final String ANDROID_PACKAGE = CheckRunEnvironment.isRunningInGitHub() ? System.getProperty("androidPackage").trim() : SecretsHandler.getConfigValue("androidPackage").trim();
		public static final String IOS_BUNDLE_ID = CheckRunEnvironment.isRunningInGitHub() ? System.getProperty("iosBundleId").trim() : SecretsHandler.getConfigValue("iosBundleId").trim();
	}

	public static class AppiumServer {
		public static final int DEFAULT_PORT = 4723;
		public static final int DEFAULT_SESSION_OVERRIDE_TIMEOUT = 60;
		public static final String APPIUM_PATH = getConfigValue("appium.appiumPath", "/opt/homebrew/bin/appium");
		public static final String NODE_PATH = getConfigValue("appium.nodePath", "/opt/homebrew/bin/node");
		public static final String NPM_PATH = getConfigValue("appium.npmPath", "/opt/homebrew/bin/npm");

		private static String getConfigValue(String key, String defaultValue) {
			if (CheckRunEnvironment.isRunningInGitHub()) {
				String value = System.getProperty(key.replace("appium.", ""));
				return value != null ? value : defaultValue;
			} else {
				String value = SecretsHandler.getConfigValue(key);
				return value != null ? value : defaultValue;
			}
		}
	}

	public static class AppUserData {
		public static final String USER_PHONE_NUMBER = "9999999999";
		public static final String USER_OTP = "123456";
	}
}
