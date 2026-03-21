package com.cashi.mobile.screens;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Appium Screen Object for the Home screen.
 * Contains ONLY actions and locators — no assertions.
 */
public class HomeScreen {

    private final AppiumDriver driver;

    public HomeScreen(AppiumDriver driver) {
        this.driver = driver;
    }

    @Step("Get welcome message")
    public String getWelcomeMessage() {
        return driver.findElement(AppiumBy.accessibilityId("welcome-text")).getText();
    }

    @Step("Get account balance")
    public String getBalance() {
        return driver.findElement(AppiumBy.accessibilityId("balance-text")).getText();
    }

    @Step("Tap Send Money")
    public void tapSendMoney() {
        driver.findElement(AppiumBy.accessibilityId("send-money-button")).click();
    }

    @Step("Tap Settings")
    public void tapSettings() {
        driver.findElement(AppiumBy.accessibilityId("settings-button")).click();
    }

    public boolean isLoaded() {
        return !driver.findElements(AppiumBy.accessibilityId("welcome-text")).isEmpty();
    }
}
