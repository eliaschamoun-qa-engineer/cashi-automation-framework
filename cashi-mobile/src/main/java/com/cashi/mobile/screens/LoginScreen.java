package com.cashi.mobile.screens;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Appium Screen Object for the Login screen.
 * Contains ONLY actions and locators — no assertions.
 */
public class LoginScreen {

    private final AppiumDriver driver;

    public LoginScreen(AppiumDriver driver) {
        this.driver = driver;
    }

    @Step("Enter email: {email}")
    public LoginScreen enterEmail(String email) {
        driver.findElement(AppiumBy.accessibilityId("email-input")).sendKeys(email);
        return this;
    }

    @Step("Enter password")
    public LoginScreen enterPassword(String password) {
        driver.findElement(AppiumBy.accessibilityId("password-input")).sendKeys(password);
        return this;
    }

    @Step("Tap login button")
    public HomeScreen tapLogin() {
        driver.findElement(AppiumBy.accessibilityId("login-button")).click();
        return new HomeScreen(driver);
    }

    @Step("Perform full login with {email}")
    public HomeScreen loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        return tapLogin();
    }

    public String getErrorMessage() {
        return driver.findElement(AppiumBy.accessibilityId("error-message")).getText();
    }

    public boolean isErrorVisible() {
        return !driver.findElements(AppiumBy.accessibilityId("error-message")).isEmpty();
    }
}
