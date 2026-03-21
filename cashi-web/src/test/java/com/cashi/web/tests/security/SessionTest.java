package com.cashi.web.tests.security;

import com.cashi.web.BaseWebTest;
import com.cashi.web.pages.LoginPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
@Tag("web")
@Epic("Security")
@Feature("Session Management")
class SessionTest extends BaseWebTest {

    @Test
    void shouldRedirectToLoginAfterSessionExpiry() {
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.navigate(getBaseUrl());
        loginPage.loginAs("user@cashi.com", "ValidP@ss123");

        // Clear cookies to simulate session expiry
        getPage().context().clearCookies();
        getPage().navigate(getBaseUrl() + "/dashboard");

        assertThat(getPage().url()).contains("/login");
    }

    @Test
    void shouldInvalidateSessionAfterLogout() {
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.navigate(getBaseUrl());
        loginPage.loginAs("user@cashi.com", "ValidP@ss123");

        getPage().navigate(getBaseUrl() + "/logout");
        getPage().navigate(getBaseUrl() + "/dashboard");

        assertThat(getPage().url()).contains("/login");
    }
}
