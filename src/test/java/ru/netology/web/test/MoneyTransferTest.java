package ru.netology.web.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.page.LoginPageV3;
import ru.netology.web.page.VerificationPage;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.page;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Тестирование перевода денег между картами")
class MoneyTransferTest {
    private DataHelper.AuthInfo authInfo;
    private DataHelper.VerificationCode verificationCode;

    @BeforeEach
    void setUp() {
        authInfo = DataHelper.getAuthInfo();
        verificationCode = DataHelper.getVerificationCodeFor(authInfo);
    }

    private DashboardPage login() {
        open("http://localhost:9999");
        LoginPageV3 loginPage = page(LoginPageV3.class);
        VerificationPage verificationPage = loginPage.validLogin(authInfo);
        return verificationPage.validVerify(verificationCode);
    }
    // Проверка, что балансы карт изменились после перевода
    private void verifyBalancesChanged(DashboardPage dashboardPage,
                                       String firstCardDigits, int firstCardInitialBalance,
                                       String secondCardDigits, int secondCardInitialBalance,
                                       int transferAmount) {
        int expectedFirstCardBalance = firstCardInitialBalance + transferAmount;
        int expectedSecondCardBalance = secondCardInitialBalance - transferAmount;

        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCardDigits);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCardDigits);

        assertEquals(expectedFirstCardBalance, actualFirstCardBalance,
            String.format("Баланс карты %s не совпадает", firstCardDigits));
        assertEquals(expectedSecondCardBalance, actualSecondCardBalance,
            String.format("Баланс карты %s не совпадает", secondCardDigits));
    }

    // Проверка, что балансы карт не изменились
    private void verifyBalancesNotChanged(DashboardPage dashboardPage,
                                          String firstCardDigits, int firstCardInitialBalance,
                                          String secondCardDigits, int secondCardInitialBalance) {
        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCardDigits);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCardDigits);

        assertEquals(firstCardInitialBalance, actualFirstCardBalance,
            String.format("Баланс карты %s изменился", firstCardDigits));
        assertEquals(secondCardInitialBalance, actualSecondCardBalance,
            String.format("Баланс карты %s изменился", secondCardDigits));
    }

    @Test
    @DisplayName("Успешный перевод 1000 рублей с карты 2 на карту 1")
    void shouldTransferMoneyFromSecondToFirstCard() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        int initialSecondCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS);

        var transferPage = dashboardPage.selectCardForTopUp(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        dashboardPage = transferPage.transferFrom(
                DataHelper.CardInfo.SECOND_CARD_NUMBER,
                1000
        );

        verifyBalancesChanged(
                dashboardPage,
                DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS, initialFirstCardBalance,
                DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS, initialSecondCardBalance,
                1000
        );
    }

    @Test
    @DisplayName("Успешный перевод 500 рублей с карты 1 на карту 2")
    void shouldTransferMoneyFromFirstToSecondCard() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        int initialSecondCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS);

        var transferPage = dashboardPage.selectCardForTopUp(DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS);
        dashboardPage = transferPage.transferFrom(
                DataHelper.CardInfo.FIRST_CARD_NUMBER,
                500
        );

        verifyBalancesChanged(
                dashboardPage,
                DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS, initialSecondCardBalance,
                DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS, initialFirstCardBalance,
                500
        );
    }

    @Test
    @DisplayName("Перевод на ту же самую карту")
    void shouldNotTransferToSameCard() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        int initialSecondCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS);

        var transferPage = dashboardPage.selectCardForTopUp(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        transferPage.transferFrom(DataHelper.CardInfo.FIRST_CARD_NUMBER, 100);

        verifyBalancesNotChanged(
                dashboardPage,
                DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS, initialFirstCardBalance,
                DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS, initialSecondCardBalance
        );
    }

    @Test
    @DisplayName("Отмена операции перевода после заполнения формы")
    void shouldCancelTransferOperation() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        int initialSecondCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS);

        var transferPage = dashboardPage.selectCardForTopUp(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        transferPage.fillForm(DataHelper.CardInfo.SECOND_CARD_NUMBER, 1000);
        dashboardPage = transferPage.cancel();

        verifyBalancesNotChanged(
                dashboardPage,
                DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS, initialFirstCardBalance,
                DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS, initialSecondCardBalance
        );
    }

    @Test
    @DisplayName("Перевод суммы, превышающей баланс карты")
    void shouldNotTransferAmountExceedingBalance() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        int initialSecondCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS);

        int excessiveAmount = initialSecondCardBalance + 5000;

        var transferPage = dashboardPage.selectCardForTopUp(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        dashboardPage = transferPage.transferFrom(
                DataHelper.CardInfo.SECOND_CARD_NUMBER,
                excessiveAmount
        );

        verifyBalancesNotChanged(
                dashboardPage,
                DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS, initialFirstCardBalance,
                DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS, initialSecondCardBalance
        );
    }

    @Test
    @DisplayName("Валидация: перевод с пустой суммой")
    void shouldNotTransferWithEmptyAmount() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        int initialSecondCardBalance = dashboardPage.getCardBalance(DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS);

        var transferPage = dashboardPage.selectCardForTopUp(DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS);
        transferPage.fillFrom(DataHelper.CardInfo.SECOND_CARD_NUMBER);
        transferPage.fillAmount(0);
        dashboardPage = transferPage.transfer();

        verifyBalancesNotChanged(
                dashboardPage,
                DataHelper.CardInfo.FIRST_CARD_LAST_DIGITS, initialFirstCardBalance,
                DataHelper.CardInfo.SECOND_CARD_LAST_DIGITS, initialSecondCardBalance
        );
    }
}