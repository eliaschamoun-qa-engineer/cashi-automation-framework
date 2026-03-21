package com.cashi.web.tests.security;

import com.cashi.core.utils.security.SecurityPayloads;
import com.cashi.web.BaseWebTest;
import com.cashi.web.pages.LoginPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
@Tag("web")
@Epic("Security")
@Feature("Input Sanitization")
class InputSanitizationTest extends BaseWebTest {

    @ParameterizedTest(name = "XSS payload #{index}: {0}")
    @MethodSource("xssPayloads")
    void shouldNotExecuteXssInLoginField(String payload) {
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.navigate(getBaseUrl());
        loginPage.enterEmail(payload);
        loginPage.enterPassword("test");
        loginPage.clickLogin();

        String pageContent = getPage().content();
        assertThat(pageContent)
                .doesNotContain("<script>")
                .doesNotContain("onerror=")
                .doesNotContain("javascript:");
    }

    @ParameterizedTest(name = "SQLi payload #{index}: {0}")
    @MethodSource("sqliPayloads")
    void shouldNotBeVulnerableToSqlInjection(String payload) {
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.navigate(getBaseUrl());
        loginPage.enterEmail(payload);
        loginPage.enterPassword("test");
        loginPage.clickLogin();

        String pageContent = getPage().content().toLowerCase();
        assertThat(pageContent)
                .doesNotContain("sql")
                .doesNotContain("syntax")
                .doesNotContain("mysql")
                .doesNotContain("oracle");
    }

    static Stream<String> xssPayloads() {
        return SecurityPayloads.xssVectors().stream();
    }

    static Stream<String> sqliPayloads() {
        return SecurityPayloads.sqliVectors().stream();
    }
}
