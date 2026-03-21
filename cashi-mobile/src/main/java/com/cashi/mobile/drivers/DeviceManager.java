package com.cashi.mobile.drivers;

import com.cashi.core.config.ConfigManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages Appium driver creation, routing to local Appium Server or cloud grid (BrowserStack/SauceLabs).
 * Reads -Ddevice.provider to determine target and capabilities from JSON config files.
 */
public class DeviceManager {

    private static final Logger log = LoggerFactory.getLogger(DeviceManager.class);

    public static AppiumDriver createDriver(String capabilitiesFile) {
        String provider = System.getProperty("device.provider", "local");
        DesiredCapabilities caps = loadCapabilities(capabilitiesFile);

        try {
            URL serverUrl = resolveServerUrl(provider);
            String platformName = caps.getCapability("platformName").toString().toLowerCase();

            log.info("Creating {} driver via {} at {}", platformName, provider, serverUrl);

            if ("android".equals(platformName)) {
                return new AndroidDriver(serverUrl, caps);
            } else if ("ios".equals(platformName)) {
                return new IOSDriver(serverUrl, caps);
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + platformName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Appium driver", e);
        }
    }

    private static URL resolveServerUrl(String provider) throws Exception {
        return switch (provider) {
            case "local" -> new URL(ConfigManager.get("appium.local.url", "http://127.0.0.1:4723"));
            case "browserstack" -> new URL(ConfigManager.get("browserstack.url"));
            case "saucelabs" -> new URL(ConfigManager.get("saucelabs.url"));
            default -> throw new IllegalArgumentException("Unknown device provider: " + provider);
        };
    }

    private static DesiredCapabilities loadCapabilities(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        DesiredCapabilities caps = new DesiredCapabilities();

        try (InputStream is = DeviceManager.class.getClassLoader()
                .getResourceAsStream("capabilities/" + fileName)) {
            if (is == null) {
                throw new IllegalStateException("Capabilities file not found: capabilities/" + fileName);
            }
            JsonNode root = mapper.readTree(is);
            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                caps.setCapability(field.getKey(), field.getValue().asText());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load capabilities: " + fileName, e);
        }

        return caps;
    }
}
