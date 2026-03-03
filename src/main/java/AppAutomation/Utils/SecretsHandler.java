package AppAutomation.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SecretsHandler {

	private static Properties propertiesInstance;

	private SecretsHandler() {}

	private static Properties getInstance() {
		if (propertiesInstance == null) {
			setInstance();
		}
		return propertiesInstance;
	}

	private static void setInstance() {
		try {
			String CONFIG_PATH;
			if (CheckRunEnvironment.isRunningInGitHub()) {
				CONFIG_PATH = System.getProperty("CONFIG_PATH");
			} else {
				if (!CheckRunEnvironment.isLocalProd()) {
					CONFIG_PATH = System.getProperty("user.dir") + "/src/main/java/Resources/config-stage.properties";
				} else {
					CONFIG_PATH = System.getProperty("user.dir") + "/src/main/java/Resources/config-prod.properties";
				}
			}

			propertiesInstance = new Properties();
			FileInputStream propertiesLoader = new FileInputStream(CONFIG_PATH);
			propertiesInstance.load(propertiesLoader);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String getConfigValue(String key) {
		return getInstance().getProperty(key);
	}
}
