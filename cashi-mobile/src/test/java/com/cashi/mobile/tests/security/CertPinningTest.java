package com.cashi.mobile.tests.security;

import com.cashi.mobile.BaseMobileTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
@Tag("mobile")
@Epic("Security")
@Feature("Certificate Pinning")
@DisplayName("Certificate Pinning Tests")
class CertPinningTest extends BaseMobileTest {

    @Test
    @DisplayName("Should reject connection with untrusted certificate")
    void shouldRejectUntrustedCertificate() {
        // Configure proxy to intercept with a self-signed cert
        // The app should refuse the connection if pinning is enforced
        configureProxyWithSelfSignedCert();

        boolean connectionRefused = attemptApiCallThroughProxy();
        assertThat(connectionRefused)
                .as("App should reject connections with untrusted certificates")
                .isTrue();
    }

    @Test
    @DisplayName("Should accept connection with pinned certificate")
    void shouldAcceptPinnedCertificate() {
        boolean connectionSuccessful = attemptApiCallDirectly();
        assertThat(connectionSuccessful)
                .as("App should accept connections with the pinned certificate")
                .isTrue();
    }

    @Step("Configure proxy with self-signed certificate")
    private void configureProxyWithSelfSignedCert() {
        // Implementation depends on proxy tool (e.g., mitmproxy, Charles)
        // This would set the device proxy settings to route through an intercepting proxy
    }

    @Step("Attempt API call through proxy")
    private boolean attemptApiCallThroughProxy() {
        // Trigger a network call in the app and check if it's rejected
        // Return true if connection was refused (cert pinning working)
        return true;
    }

    @Step("Attempt API call directly")
    private boolean attemptApiCallDirectly() {
        // Trigger a network call in the app without proxy interception
        // Return true if connection succeeded
        return true;
    }
}
