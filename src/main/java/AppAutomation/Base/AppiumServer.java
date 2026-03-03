package AppAutomation.Base;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import AppAutomation.TestData.TestData;
import AppAutomation.Utils.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AppiumServer {

    private static final ThreadLocal<AppiumDriverLocalService> serverThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, AppiumDriverLocalService> serverInstances = new ConcurrentHashMap<>();

    public static AppiumDriverLocalService startServer() {
        Log.info("Starting Appium server on thread: " + Thread.currentThread().getName());
        return startServer(TestData.AppiumServer.DEFAULT_PORT, TestData.AppiumServer.APPIUM_PATH, TestData.AppiumServer.NODE_PATH);
    }

    public static AppiumDriverLocalService startServer(int port, String appiumPath, String nodePath) {
        try {
            stopExistingServersOnPort(port);

            String serverKey = "server_" + port;
            if (serverInstances.containsKey(serverKey) && serverInstances.get(serverKey).isRunning()) {
                Log.info("Reusing existing Appium server on port " + port);
                AppiumDriverLocalService existingServer = serverInstances.get(serverKey);
                serverThreadLocal.set(existingServer);
                return existingServer;
            }

            if (!isPortAvailable(port)) {
                forceStopProcessesOnPort(port);
                Thread.sleep(2000);
            }

            AppiumServiceBuilder serviceBuilder = new AppiumServiceBuilder()
                    .usingDriverExecutable(new File(nodePath))
                    .withAppiumJS(new File(appiumPath))
                    .usingPort(port)
                    .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                    .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                    .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                    .withArgument(GeneralServerFlag.ALLOW_INSECURE, "chromedriver_autodownload")
                    .withArgument(GeneralServerFlag.ALLOW_INSECURE, "adb_shell")
                    .withTimeout(Duration.ofSeconds(TestData.AppiumServer.DEFAULT_SESSION_OVERRIDE_TIMEOUT));

            Map<String, String> environment = new HashMap<>(System.getenv());

            if (TestData.AppiumServer.NPM_PATH != null && !TestData.AppiumServer.NPM_PATH.isEmpty()) {
                String currentPath = environment.get("PATH");
                String npmDir = new File(TestData.AppiumServer.NPM_PATH).getParent();
                environment.put("PATH", npmDir + (currentPath != null ? ":" + currentPath : ""));
            }

            String androidHome = getAndroidSdkPath();
            if (androidHome != null) {
                environment.put("ANDROID_HOME", androidHome);
                environment.put("ANDROID_SDK_ROOT", androidHome);
                String currentPath = environment.get("PATH");
                String androidTools = androidHome + "/platform-tools:" + androidHome + "/emulator:" + androidHome + "/tools:" + androidHome + "/tools/bin";
                environment.put("PATH", androidTools + (currentPath != null ? ":" + currentPath : ""));
            }

            serviceBuilder.withEnvironment(environment);

            AppiumDriverLocalService service = AppiumDriverLocalService.buildService(serviceBuilder);
            service.start();

            serverInstances.put(serverKey, service);
            serverThreadLocal.set(service);

            Log.info("Appium server started on port: " + port + " | URL: " + service.getUrl());
            return service;

        } catch (Exception e) {
            Log.error("Failed to start Appium server on port " + port + " - " + e.getMessage());
            throw new RuntimeException("Failed to start Appium server", e);
        }
    }

    private static String getAndroidSdkPath() {
        String androidHome = System.getenv("ANDROID_HOME");
        if (androidHome != null && new File(androidHome).exists()) return androidHome;

        androidHome = System.getenv("ANDROID_SDK_ROOT");
        if (androidHome != null && new File(androidHome).exists()) return androidHome;

        String[] commonPaths = {
            System.getProperty("user.home") + "/Library/Android/sdk",
            System.getProperty("user.home") + "/Android/Sdk",
            "/usr/local/android-sdk",
            "/opt/android-sdk"
        };

        for (String path : commonPaths) {
            if (new File(path).exists() && new File(path + "/platform-tools").exists()) return path;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("which", "adb");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String adbPath = reader.readLine();
            reader.close();
            if (process.waitFor() == 0 && adbPath != null && !adbPath.trim().isEmpty()) {
                File sdkDir = new File(adbPath.trim()).getParentFile().getParentFile();
                if (sdkDir != null && sdkDir.exists()) return sdkDir.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.debug("Could not find Android SDK via 'which adb': " + e.getMessage());
        }

        return null;
    }

    private static void stopExistingServersOnPort(int port) {
        try {
            String serverKey = "server_" + port;
            if (serverInstances.containsKey(serverKey)) {
                AppiumDriverLocalService existingServer = serverInstances.get(serverKey);
                if (existingServer.isRunning()) existingServer.stop();
                serverInstances.remove(serverKey);
            }
            if (isAppiumServerRunningOnPort(port)) forceStopProcessesOnPort(port);
        } catch (Exception e) {
            Log.warn("Error during existing server cleanup on port " + port + ": " + e.getMessage());
        }
    }

    private static boolean isAppiumServerRunningOnPort(int port) {
        try {
            URL url = new URL("http://localhost:" + port + "/status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static void forceStopProcessesOnPort(int port) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("lsof", "-ti", ":" + port);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String pid = line.trim();
                if (!pid.isEmpty() && Pattern.matches("\\d+", pid)) {
                    new ProcessBuilder("kill", "-9", pid).start().waitFor();
                }
            }
            reader.close();
            process.waitFor();
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.warn("Error force stopping processes on port " + port + ": " + e.getMessage());
        }
    }

    public static void stopServer() {
        AppiumDriverLocalService service = serverThreadLocal.get();
        if (service != null && service.isRunning()) {
            Log.info("Stopping Appium server for thread: " + Thread.currentThread().getName());
            service.stop();
            serverThreadLocal.remove();
        }
    }

    public static void stopAllServers() {
        for (AppiumDriverLocalService service : serverInstances.values()) {
            if (service.isRunning()) {
                try { service.stop(); } catch (Exception e) { Log.error("Error stopping server: " + e.getMessage()); }
            }
        }
        serverInstances.clear();
        serverThreadLocal.remove();
    }

    public static AppiumDriverLocalService getCurrentServer() {
        return serverThreadLocal.get();
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
