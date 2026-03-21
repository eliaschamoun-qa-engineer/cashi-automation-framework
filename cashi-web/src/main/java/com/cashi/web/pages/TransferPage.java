package com.cashi.web.pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

/**
 * Playwright Page Object for the Transfer/Send Money page.
 * Contains ONLY actions and locators — no assertions.
 */
public class TransferPage {

    private final Page page;

    private static final String RECIPIENT_INPUT = "#recipient";
    private static final String AMOUNT_INPUT = "#amount";
    private static final String SEND_BUTTON = "button.confirm-send";
    private static final String CONFIRMATION_MESSAGE = ".transfer-confirmation";
    private static final String ERROR_MESSAGE = ".transfer-error";

    public TransferPage(Page page) {
        this.page = page;
    }

    @Step("Enter recipient: {recipient}")
    public TransferPage enterRecipient(String recipient) {
        page.locator(RECIPIENT_INPUT).fill(recipient);
        return this;
    }

    @Step("Enter amount: {amount}")
    public TransferPage enterAmount(String amount) {
        page.locator(AMOUNT_INPUT).fill(amount);
        return this;
    }

    @Step("Click send")
    public TransferPage clickSend() {
        page.locator(SEND_BUTTON).click();
        return this;
    }

    public String getConfirmationMessage() {
        return page.locator(CONFIRMATION_MESSAGE).textContent();
    }

    public String getErrorMessage() {
        return page.locator(ERROR_MESSAGE).textContent();
    }

    public boolean isConfirmationVisible() {
        return page.locator(CONFIRMATION_MESSAGE).isVisible();
    }
}
