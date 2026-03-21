package com.cashi.web.tests.security;

import com.cashi.web.BaseWebTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
@Tag("web")
@Epic("Security")
@Feature("Security Headers")
class SecurityHeadersTest extends BaseWebTest {

    @Test
    void shouldReturnRequiredSecurityHeaders() {
        var response = getPage().navigate(getBaseUrl() + "/login");

        Map<String, String> headers = response.headers();

        assertThat(headers.get("x-content-type-options"))
                .as("X-Content-Type-Options")
                .isEqualTo("nosniff");

        assertThat(headers.get("x-frame-options"))
                .as("X-Frame-Options")
                .isIn("DENY", "SAMEORIGIN");
    }

    @Test
    void shouldEnforceStrictTransportSecurity() {
        var response = getPage().navigate(getBaseUrl() + "/login");
        Map<String, String> headers = response.headers();

        assertThat(headers.get("strict-transport-security"))
                .as("Strict-Transport-Security")
                .isNotNull()
                .contains("max-age=");
    }
}
