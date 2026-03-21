package com.cashi.mobile.screens;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Appium Screen Object for the Biometrics screen.
 * Contains ONLY actions and locators — no assertions.
 */
public class BiometricsScreen {

    private final AppiumDriver driver;

    public BiometricsScreen(AppiumDriver driver) {
        this.driver = driver;
    }

    @Step("Enable biometric authentication")
    public BiometricsScreen enableBiometrics() {
        driver.findElement(AppiumBy.accessibilityId("enable-biometrics-toggle")).click();
        return this;
    }

    @Step("Confirm biometric enrollment")
    public void confirmEnrollment() {
        driver.findElement(AppiumBy.accessibilityId("confirm-biometrics-button")).click();
    }

    public boolean isBiometricsEnabled() {
        String value = driver.findElement(AppiumBy.accessibilityId("enable-biometrics-toggle"))
                .getAttribute("checked");
        return "true".equals(value);
    }

    public String getStatusMessage() {
        return driver.findElement(AppiumBy.accessibilityId("biometrics-status")).getText();
    }
}
