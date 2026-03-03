package AppAutomation.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

	private static volatile Log instance;
	private static final Object lock = new Object();
	private PrintStream log;
	private String logTimeStamp = getCurrentDateTime();

	private Log() {
		try {
			log = new PrintStream(new FileOutputStream(System.getProperty("user.dir") + "/logs" + logTimeStamp + ".txt"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private static Log getInstance() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new Log();
				}
			}
		}
		return instance;
	}

	public static void info(String message) {
		Log instance = getInstance();
		instance.log.println("[INFO] " + getCurrentDateTime() + " - " + message);
		System.out.println("[INFO] " + getCurrentDateTime() + " - " + message);
	}

	public static void warn(String message) {
		Log instance = getInstance();
		instance.log.println("[WARNING] " + getCurrentDateTime() + " - " + message);
		Log.info("[WARNING] " + getCurrentDateTime() + " - " + message);
	}

	public static void error(String message) {
		Log instance = getInstance();
		instance.log.println("[ERROR] " + getCurrentDateTime() + " - " + message);
		Log.info("[ERROR] " + getCurrentDateTime() + " - " + message);
	}

	public static void debug(String message) {
		Log instance = getInstance();
		instance.log.println("[DEBUG] " + getCurrentDateTime() + " - " + message);
	}

	public static String getLoggerFileName() {
		Log instance = getInstance();
		return System.getProperty("user.dir") + "/logs" + instance.logTimeStamp + ".txt";
	}

	public static PrintStream getLog() {
		Log instance = getInstance();
		return instance.log;
	}

	private static String getCurrentDateTime() {
		LocalDateTime now = LocalDateTime.now();
		if (CheckRunEnvironment.isRunningInGitHub()) {
			now = now.plusHours(5).plusMinutes(30);
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
		return now.format(formatter);
	}
}
