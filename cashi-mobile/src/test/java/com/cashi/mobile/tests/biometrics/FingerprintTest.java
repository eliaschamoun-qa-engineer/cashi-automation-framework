package com.cashi.mobile.tests.biometrics;

import com.cashi.mobile.BaseMobileTest;
import com.cashi.mobile.screens.BiometricsScreen;
import com.cashi.mobile.screens.HomeScreen;
import com.cashi.mobile.screens.LoginScreen;
import com.cashi.core.data.factories.UserFactory;
import com.cashi.core.models.UserAccount;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("regression")
@Tag("mobile")
@Epic("Security")
@Feature("Biometrics")
@DisplayName("Fingerprint Biometric Tests")
class FingerprintTest extends BaseMobileTest {

    @Test
    @DisplayName("Should display biometric prompt after successful login")
    void shouldDisplayBiometricPromptAfterLogin() {
        UserAccount user = UserFactory.aDefaultUser().build();

        LoginScreen loginScreen = new LoginScreen(driver());
        loginScreen.enterEmail(user.getEmail());
        loginScreen.enterPassword("ValidP@ss1");
        loginScreen.tapLoginButton();

        BiometricsScreen biometricsScreen = new BiometricsScreen(driver());
        assertThat(biometricsScreen.isBiometricPromptDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Should allow user to skip biometrics setup")
    void shouldAllowSkippingBiometrics() {
        UserAccount user = UserFactory.aDefaultUser().build();

        LoginScreen loginScreen = new LoginScreen(driver());
        loginScreen.enterEmail(user.getEmail());
        loginScreen.enterPassword("ValidP@ss1");
        loginScreen.tapLoginButton();

        BiometricsScreen biometricsScreen = new BiometricsScreen(driver());
        biometricsScreen.tapSkipBiometrics();

        HomeScreen homeScreen = new HomeScreen(driver());
        assertThat(homeScreen.getWelcomeText())
                .contains(user.getFirstName());
    }

    @Test
    @DisplayName("Should authenticate with fingerprint after enrollment")
    void shouldAuthenticateWithFingerprint() {
        UserAccount user = UserFactory.aDefaultUser().build();

        LoginScreen loginScreen = new LoginScreen(driver());
        loginScreen.enterEmail(user.getEmail());
        loginScreen.enterPassword("ValidP@ss1");
        loginScreen.tapLoginButton();

        BiometricsScreen biometricsScreen = new BiometricsScreen(driver());
        biometricsScreen.tapUseBiometrics();

        assertThat(biometricsScreen.getBiometricStatusText())
                .containsIgnoringCase("enabled");
    }
}
