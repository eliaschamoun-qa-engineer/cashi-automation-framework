package com.cashi.web.pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

/**
 * Playwright Page Object for the Login page.
 * Contains ONLY actions and locators — no assertions.
 */
public class LoginPage {

    private final Page page;

    private static final String EMAIL_INPUT = "#email";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "button[type='submit']";
    private static final String ERROR_MESSAGE = ".error-message";

    public LoginPage(Page page) {
        this.page = page;
    }

    @Step("Navigate to login page")
    public LoginPage navigate(String baseUrl) {
        page.navigate(baseUrl + "/login");
        return this;
    }

    @Step("Enter email: {email}")
    public LoginPage enterEmail(String email) {
        page.locator(EMAIL_INPUT).fill(email);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        page.locator(PASSWORD_INPUT).fill(password);
        return this;
    }

    @Step("Click login button")
    public void clickLogin() {
        page.locator(LOGIN_BUTTON).click();
    }

    @Step("Perform full login with {email}")
    public DashboardPage loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        return new DashboardPage(page);
    }

    public String getErrorMessage() {
        return page.locator(ERROR_MESSAGE).textContent();
    }

    public boolean isErrorVisible() {
        return page.locator(ERROR_MESSAGE).isVisible();
    }
}
