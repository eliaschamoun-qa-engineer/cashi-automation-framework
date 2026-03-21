package com.cashi.web.tests.security;

import com.cashi.web.BaseWebTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
@Tag("web")
@Epic("Security")
@Feature("Authorization Boundaries")
class AuthBoundaryTest extends BaseWebTest {

    @Test
    void shouldReturn401ForUnauthenticatedDashboardAccess() {
        getPage().navigate(getBaseUrl() + "/dashboard");

        String currentUrl = getPage().url();
        assertThat(currentUrl).contains("/login");
    }

    @Test
    void shouldReturn403ForUnauthorizedAdminAccess() {
        getPage().navigate(getBaseUrl() + "/admin");

        String currentUrl = getPage().url();
        assertThat(currentUrl).doesNotContain("/admin");
    }
}
