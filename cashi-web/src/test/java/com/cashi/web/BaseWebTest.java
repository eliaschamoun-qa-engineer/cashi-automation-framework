package com.cashi.web;

import com.cashi.core.config.ConfigManager;
import com.cashi.core.utils.BaseCoreTest;
import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Base test class for all Playwright web tests.
 * Extends BaseCoreTest to inherit environment config, Allure lifecycle, and prod safety guards.
 * Manages ThreadLocal Playwright browser contexts for parallel-safe execution.
 */
public abstract class BaseWebTest extends BaseCoreTest {

    private static final Logger log = LoggerFactory.getLogger(BaseWebTest.class);

    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    @BeforeEach
    void setUpBrowser() {
        Playwright playwright = Playwright.create();
        playwrightThreadLocal.set(playwright);

        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
        browserThreadLocal.set(browser);

        BrowserContext context = browser.newContext();
        contextThreadLocal.set(context);

        Page page = context.newPage();
        pageThreadLocal.set(page);

        log.info("Playwright browser context created on thread: {}", Thread.currentThread().getName());
    }

    @AfterEach
    void tearDownBrowser() {
        Page page = pageThreadLocal.get();
        if (page != null) {
            try {
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                Allure.addAttachment("Screenshot", "image/png",
                        new ByteArrayInputStream(screenshot), "png");
            } catch (Exception e) {
                log.warn("Failed to capture screenshot: {}", e.getMessage());
            }
        }

        BrowserContext context = contextThreadLocal.get();
        if (context != null) context.close();

        Browser browser = browserThreadLocal.get();
        if (browser != null) browser.close();

        Playwright playwright = playwrightThreadLocal.get();
        if (playwright != null) playwright.close();

        pageThreadLocal.remove();
        contextThreadLocal.remove();
        browserThreadLocal.remove();
        playwrightThreadLocal.remove();

        log.info("Playwright browser context closed on thread: {}", Thread.currentThread().getName());
    }

    protected Page getPage() {
        return pageThreadLocal.get();
    }

    protected String getBaseUrl() {
        return ConfigManager.get("web.base.url");
    }
}
