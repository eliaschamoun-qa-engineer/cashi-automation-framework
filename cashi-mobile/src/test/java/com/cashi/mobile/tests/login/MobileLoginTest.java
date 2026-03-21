package com.cashi.mobile.tests.login;

import com.cashi.mobile.BaseMobileTest;
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

@Tag("smoke")
@Tag("mobile")
@Epic("Authentication")
@Feature("Mobile Login")
@DisplayName("Mobile Login Tests")
class MobileLoginTest extends BaseMobileTest {

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginWithValidCredentials() {
        UserAccount user = UserFactory.aDefaultUser().build();

        LoginScreen loginScreen = new LoginScreen(driver());
        loginScreen.enterEmail(user.getEmail());
        loginScreen.enterPassword("ValidP@ss1");
        loginScreen.tapLoginButton();

        HomeScreen homeScreen = new HomeScreen(driver());
        assertThat(homeScreen.getWelcomeText())
                .contains(user.getFirstName());
    }

    @Test
    @DisplayName("Should show error for invalid credentials")
    void shouldShowErrorForInvalidCredentials() {
        LoginScreen loginScreen = new LoginScreen(driver());
        loginScreen.enterEmail("invalid@test.com");
        loginScreen.enterPassword("wrongpassword");
        loginScreen.tapLoginButton();

        assertThat(loginScreen.getErrorMessage())
                .isNotEmpty()
                .containsIgnoringCase("invalid");
    }

    @Test
    @DisplayName("Should show validation error for empty email")
    void shouldShowValidationErrorForEmptyEmail() {
        LoginScreen loginScreen = new LoginScreen(driver());
        loginScreen.enterPassword("somepassword");
        loginScreen.tapLoginButton();

        assertThat(loginScreen.getErrorMessage())
                .isNotEmpty();
    }
}
