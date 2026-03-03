package AppAutomation.Utils;

public class CheckRunEnvironment {

	public static boolean isRunningInGitHub() {
		return Boolean.parseBoolean(System.getenv("IS_GITHUB"));
	}

	public static boolean isProd() {
		return Boolean.parseBoolean(System.getenv("IS_PROD"));
	}

	public static boolean isLocalProd() {
		if (isRunningInGitHub()) return false;
		return isProd();
	}
}
