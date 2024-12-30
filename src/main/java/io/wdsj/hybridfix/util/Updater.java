package io.wdsj.hybridfix.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.wdsj.hybridfix.HybridFix;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class Updater {
    private static String currentVersion = HybridFix.VERSION;
    private static String latestVersion;
    private static boolean isUpdateAvailable = false;
    private static boolean isErred = false;
    private static final String RELEASE_URL = "https://api.github.com/repos/HaHaWTH/HybridFix/releases/latest";

    /**
     * Check if there is an update available
     * Note: This method will perform a network request!
     * @return true if there is an update available, false otherwise
     */
    public static boolean isUpdateAvailable() {
        currentVersion = HybridFix.VERSION;
        URI uri = URI.create(RELEASE_URL);
        try {
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                String latest = jsonObject.get("tag_name").getAsString();
                latestVersion = latest;
                isUpdateAvailable = !currentVersion.equals(latest);
                reader.close();
                return isUpdateAvailable;
            }
        } catch (Exception e) {
            isErred = true;
            isUpdateAvailable = false;
            return false;
        }
    }

    public static String getLatestVersion() {
        return latestVersion;
    }
    public static String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Returns true if there is an update available, false otherwise
     * Must be called after {@link Updater#isUpdateAvailable()}
     * @return A boolean indicating whether there is an update available
     */
    public static boolean hasUpdate() {
        return isUpdateAvailable;
    }

    public static boolean isErred() {
        return isErred;
    }
}
