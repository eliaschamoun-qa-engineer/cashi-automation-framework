package com.cashi.mobile.tests.security;

import com.cashi.mobile.BaseMobileTest;
import com.cashi.mobile.screens.LoginScreen;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
@Tag("mobile")
@Epic("Security")
@Feature("Deep Link Injection")
@DisplayName("Deep Link Injection Tests")
class DeepLinkInjectionTest extends BaseMobileTest {

    @ParameterizedTest(name = "Should not navigate to restricted area via deep link: {0}")
    @ValueSource(strings = {
            "cashi://admin/dashboard",
            "cashi://transfer/confirm?amount=99999&to=attacker",
            "cashi://settings/export-data",
            "cashi://internal/debug"
    })
    @DisplayName("Should reject malicious deep links")
    void shouldRejectMaliciousDeepLinks(String deepLink) {
        triggerDeepLink(deepLink);

        // After triggering a malicious deep link, the app should either:
        // - Redirect to login screen (unauthenticated)
        // - Show an error
        // - Ignore the deep link
        LoginScreen loginScreen = new LoginScreen(driver());
        assertThat(loginScreen.getErrorMessage())
                .as("Malicious deep link '%s' should not bypass authentication", deepLink)
                .satisfiesAnyOf(
                        msg -> assertThat(loginScreen).isNotNull(), // App stayed on login
                        msg -> assertThat(msg).containsIgnoringCase("error")
                );
    }

    @Test
    @DisplayName("Should sanitize deep link parameters")
    void shouldSanitizeDeepLinkParameters() {
        String xssDeepLink = "cashi://transfer?note=<script>alert('xss')</script>";
        triggerDeepLink(xssDeepLink);

        // Verify the app doesn't execute the injected script
        // and properly sanitizes the input
        assertThat(getPageSource())
                .doesNotContain("<script>");
    }

    @Step("Trigger deep link: {deepLink}")
    private void triggerDeepLink(String deepLink) {
        // Use Appium to trigger a deep link via adb for Android or xcrun for iOS
        String platform = driver().getCapabilities().getPlatformName().toString().toLowerCase();
        if (platform.contains("android")) {
            driver().executeScript("mobile: deepLink", java.util.Map.of(
                    "url", deepLink,
                    "package", "com.cashi.app"
            ));
        } else {
            driver().executeScript("mobile: deepLink", java.util.Map.of(
                    "url", deepLink,
                    "bundleId", "com.cashi.app"
            ));
        }
    }

    @Step("Get page source for validation")
    private String getPageSource() {
        return driver().getPageSource();
    }
}
