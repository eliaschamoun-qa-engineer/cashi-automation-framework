package com.cashi.web.tests.transfers;

import com.cashi.web.BaseWebTest;
import com.cashi.web.pages.DashboardPage;
import com.cashi.web.pages.LoginPage;
import com.cashi.web.pages.TransferPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("regression")
@Tag("web")
@Epic("Payments")
@Feature("Send Money")
class SendMoneyTest extends BaseWebTest {

    private DashboardPage dashboard;

    @BeforeEach
    void loginAndNavigate() {
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.navigate(getBaseUrl());
        dashboard = loginPage.loginAs("sender@cashi.com", "SenderP@ss123");
    }

    @ParameterizedTest(name = "Transfer {0} → expects {1}")
    @MethodSource("transferAmounts")
    void shouldTransferMoney(BigDecimal amount, String expectedResult) {
        TransferPage transferPage = dashboard.clickSendMoney();
        transferPage.enterRecipient("recipient@cashi.com");
        transferPage.enterAmount(amount.toPlainString());
        transferPage.clickSend();

        if ("COMPLETED".equals(expectedResult)) {
            assertThat(transferPage.isConfirmationVisible()).isTrue();
        } else {
            assertThat(transferPage.getErrorMessage()).isNotEmpty();
        }
    }

    static Stream<Arguments> transferAmounts() {
        return Stream.of(
                Arguments.of(new BigDecimal("100.00"), "COMPLETED"),
                Arguments.of(new BigDecimal("0.01"), "COMPLETED"),
                Arguments.of(new BigDecimal("999999.99"), "LIMIT_EXCEEDED")
        );
    }
}
