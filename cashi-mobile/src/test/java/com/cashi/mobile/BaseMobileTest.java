package com.cashi.mobile;

import com.cashi.core.utils.BaseCoreTest;
import com.cashi.mobile.drivers.DeviceManager;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base test for all mobile tests. Creates an isolated AppiumDriver per test via DeviceManager.
 * Extends BaseCoreTest for config loading, prod safety, and data cleanup.
 */
public abstract class BaseMobileTest extends BaseCoreTest {

    private static final Logger log = LoggerFactory.getLogger(BaseMobileTest.class);
    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    private String capabilitiesFile = System.getProperty("caps.file", "android-emulator.json");

    protected AppiumDriver driver() {
        return DRIVER.get();
    }

    @BeforeEach
    void setUpMobileDriver() {
        AppiumDriver driver = DeviceManager.createDriver(capabilitiesFile);
        DRIVER.set(driver);
        log.info("AppiumDriver created for thread {}", Thread.currentThread().getName());
    }

    @AfterEach
    void tearDownMobileDriver(org.junit.jupiter.api.TestInfo testInfo) {
        AppiumDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                if (testInfo.getTags().contains("failed") || isTestFailed()) {
                    attachScreenshot(driver);
                }
            } finally {
                driver.quit();
                DRIVER.remove();
                log.info("AppiumDriver quit for thread {}", Thread.currentThread().getName());
            }
        }
    }

    private boolean isTestFailed() {
        // JUnit 5 doesn't directly expose pass/fail in @AfterEach,
        // AllureLifecycleExtension handles failure screenshots.
        // This is a secondary safety net using try-catch in test execution.
        return false;
    }

    private void attachScreenshot(AppiumDriver driver) {
        try {
            byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
            Allure.getLifecycle().addAttachment(
                    "Mobile Screenshot on Failure", "image/png", "png", screenshot);
        } catch (Exception e) {
            log.warn("Failed to capture mobile screenshot", e);
        }
    }
}
