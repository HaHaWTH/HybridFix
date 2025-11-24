package io.wdsj.hybridfix.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.Tags;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Updater class for checking for updates, requires semantic versioning.
 */
public class Updater {
    private static String currentVersion = HybridFix.VERSION;
    private static String latestVersion;
    private static boolean isUpdateAvailable = false;
    private static boolean isErred = false;
    private static final String RELEASE_URL = "https://api.github.com/repos/HaHaWTH/HybridFix/releases/latest";
    private static final String VERSION_CHANNEL = Tags.VERSION_CHANNEL;
    @SuppressWarnings("ConstantConditions")
    private static final boolean isDev = "dev".equalsIgnoreCase(VERSION_CHANNEL);

    /**
     * Check if there is an update available
     * Note: This method will perform a network request!
     * @return true if there is an update available, false otherwise
     */
    @SuppressWarnings("ConstantConditions")
    public static synchronized boolean isUpdateAvailable() {
        currentVersion = HybridFix.VERSION;
        try {
            URI uri = URI.create(RELEASE_URL);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("User-Agent", "HybridFix-Updater");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                isErred = true;
                return false;
            }

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                String latest = jsonObject.get("tag_name").getAsString();
                latestVersion = latest;

                int[] splitLatest = parseSemanticVersion(latest);
                int[] splitCurrent = parseSemanticVersion(currentVersion);

                int comparisonResult = compareVersions(splitLatest, splitCurrent);

                if (comparisonResult > 0) {
                    isUpdateAvailable = true;
                } else {
                    isUpdateAvailable = comparisonResult == 0 && isDev;
                }

                return isUpdateAvailable;
            }
        } catch (Exception e) {
            isErred = true;
            isUpdateAvailable = false;
            return false;
        }
    }

    private static int compareVersions(int[] remote, int[] local) {
        int length = Math.max(remote.length, local.length);
        for (int i = 0; i < length; i++) {
            int r = i < remote.length ? remote[i] : 0;
            int l = i < local.length ? local[i] : 0;

            if (r > l) return 1;
            if (r < l) return -1;
        }
        return 0;
    }

    public static String getLatestVersion() {
        return latestVersion;
    }

    public static String getCurrentVersion() {
        return currentVersion;
    }

    public static boolean hasUpdate() {
        return isUpdateAvailable;
    }

    public static boolean isErred() {
        return isErred;
    }

    public static boolean isDev() {
        return isDev;
    }

    private static int[] parseSemanticVersion(String version) {
        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }

        int hyphenIndex = version.indexOf('-');
        if (hyphenIndex != -1) {
            version = version.substring(0, hyphenIndex);
        }

        List<Integer> temp = new ArrayList<>();
        for (String versionPart : version.split("\\.")) {
            try {
                temp.add(Integer.parseInt(versionPart));
            } catch (NumberFormatException ignored) {
                temp.add(0);
            }
        }
        return temp.stream().mapToInt(Integer::intValue).toArray();
    }
}