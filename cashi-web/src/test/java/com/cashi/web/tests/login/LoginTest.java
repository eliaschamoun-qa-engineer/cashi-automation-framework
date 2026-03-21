package com.cashi.web.tests.login;

import com.cashi.web.BaseWebTest;
import com.cashi.web.pages.DashboardPage;
import com.cashi.web.pages.LoginPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("smoke")
@Tag("web")
@Epic("Authentication")
@Feature("Login")
class LoginTest extends BaseWebTest {

    @Test
    void shouldLoginWithValidCredentials() {
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.navigate(getBaseUrl());

        DashboardPage dashboard = loginPage.loginAs("valid@cashi.com", "ValidP@ss123");

        assertThat(dashboard.isLoaded()).isTrue();
        assertThat(dashboard.getWelcomeMessage()).contains("Welcome");
    }

    @Test
    void shouldShowErrorForInvalidCredentials() {
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.navigate(getBaseUrl());

        loginPage.enterEmail("invalid@cashi.com");
        loginPage.enterPassword("WrongPassword");
        loginPage.clickLogin();

        assertThat(loginPage.isErrorVisible()).isTrue();
        assertThat(loginPage.getErrorMessage()).isNotEmpty();
    }
}
