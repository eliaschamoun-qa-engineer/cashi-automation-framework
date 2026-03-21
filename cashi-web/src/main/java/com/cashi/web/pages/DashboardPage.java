package com.cashi.web.pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

/**
 * Playwright Page Object for the Dashboard page.
 * Contains ONLY actions and locators — no assertions.
 */
public class DashboardPage {

    private final Page page;

    private static final String WELCOME_HEADER = "h1.welcome";
    private static final String BALANCE_DISPLAY = ".account-balance";
    private static final String SEND_MONEY_BUTTON = "button.send-money";
    private static final String LOGOUT_BUTTON = "button.logout";

    public DashboardPage(Page page) {
        this.page = page;
    }

    @Step("Get welcome message text")
    public String getWelcomeMessage() {
        return page.locator(WELCOME_HEADER).textContent();
    }

    @Step("Get account balance")
    public String getBalance() {
        return page.locator(BALANCE_DISPLAY).textContent();
    }

    @Step("Click Send Money")
    public TransferPage clickSendMoney() {
        page.locator(SEND_MONEY_BUTTON).click();
        return new TransferPage(page);
    }

    @Step("Logout")
    public LoginPage logout() {
        page.locator(LOGOUT_BUTTON).click();
        return new LoginPage(page);
    }

    public boolean isLoaded() {
        return page.locator(WELCOME_HEADER).isVisible();
    }
}
