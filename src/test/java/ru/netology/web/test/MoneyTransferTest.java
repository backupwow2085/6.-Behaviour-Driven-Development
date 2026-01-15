package ru.netology.web.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.netology.web.data.DataHelper;
import ru.netology.web.data.DataHelper.CardInfo;
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
    private CardInfo firstCard;
    private CardInfo secondCard;

    @BeforeEach
    void setUp() {
        authInfo = DataHelper.getAuthInfo();
        verificationCode = DataHelper.getVerificationCodeFor(authInfo);
        firstCard = DataHelper.getFirstCard();
        secondCard = DataHelper.getSecondCard();
    }

    private DashboardPage login() {
        open("http://localhost:9999");
        LoginPageV3 loginPage = page(LoginPageV3.class);
        VerificationPage verificationPage = loginPage.validLogin(authInfo);
        return verificationPage.validVerify(verificationCode);
    }

    @Test
    @DisplayName("Успешный перевод 30% баланса с карты 2 на карту 1")
    void shouldTransferMoneyFromSecondToFirstCard() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int initialSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        int transferAmount = (int) (initialSecondCardBalance * 0.3);

        if (transferAmount <= 0) {
            transferAmount = 100;
        }

        var transferPage = dashboardPage.selectCardForTopUp(firstCard);
        dashboardPage = transferPage.transferFrom(
                secondCard.getCardNumber(),
                transferAmount
        );

        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        assertEquals(initialFirstCardBalance + transferAmount, actualFirstCardBalance,
                "Баланс первой карты должен увеличиться на " + transferAmount + " рублей");
        assertEquals(initialSecondCardBalance - transferAmount, actualSecondCardBalance,
                "Баланс второй карты должен уменьшиться на " + transferAmount + " рублей");
    }

    @Test
    @DisplayName("Успешный перевод половины баланса с карты 1 на карту 2")
    void shouldTransferMoneyFromFirstToSecondCard() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int initialSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        int transferAmount = initialFirstCardBalance / 2;

        if (transferAmount <= 0) {
            transferAmount = 100;
        }

        var transferPage = dashboardPage.selectCardForTopUp(secondCard);
        dashboardPage = transferPage.transferFrom(
                firstCard.getCardNumber(),
                transferAmount
        );

        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        assertEquals(initialFirstCardBalance - transferAmount, actualFirstCardBalance,
                "Баланс первой карты должен уменьшиться на " + transferAmount + " рублей");
        assertEquals(initialSecondCardBalance + transferAmount, actualSecondCardBalance,
                "Баланс второй карты должен увеличиться на " + transferAmount + " рублей");
    }

    @Test
    @DisplayName("Перевод на ту же самую карту")
    void shouldNotTransferToSameCard() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int initialSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        int transferAmount = initialFirstCardBalance / 10;

        var transferPage = dashboardPage.selectCardForTopUp(firstCard);
        dashboardPage = transferPage.transferFrom(firstCard.getCardNumber(), transferAmount);

        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        assertEquals(initialFirstCardBalance, actualFirstCardBalance,
                "Баланс первой карты не должен измениться при переводе на себя");
        assertEquals(initialSecondCardBalance, actualSecondCardBalance,
                "Баланс второй карты не должен измениться");
    }

    @Test
    @DisplayName("Отмена операции перевода после заполнения формы")
    void shouldCancelTransferOperation() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int initialSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        int transferAmount = initialSecondCardBalance / 5;

        var transferPage = dashboardPage.selectCardForTopUp(firstCard);
        transferPage.fillForm(secondCard.getCardNumber(), transferAmount);
        dashboardPage = transferPage.cancel();

        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        assertEquals(initialFirstCardBalance, actualFirstCardBalance,
                "Баланс первой карты не должен измениться после отмены");
        assertEquals(initialSecondCardBalance, actualSecondCardBalance,
                "Баланс второй карты не должен измениться после отмены");
    }


    @Test
    @DisplayName("Перевод с пустой суммой")
    void shouldNotTransferWithEmptyAmount() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int initialSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        var transferPage = dashboardPage.selectCardForTopUp(firstCard);

        dashboardPage = transferPage.transferFrom(secondCard.getCardNumber(), 0);

        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        assertEquals(initialFirstCardBalance, actualFirstCardBalance,
                "Баланс первой карты не должен измениться при переводе нулевой суммы");
        assertEquals(initialSecondCardBalance, actualSecondCardBalance,
                "Баланс второй карты не должен измениться при переводе нулевой суммы");
    }

    @Test
    @DisplayName("Перевод суммы, превышающей баланс карты")
    void shouldNotTransferAmountExceedingBalance() {
        DashboardPage dashboardPage = login();

        int initialFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int initialSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        int excessiveAmount = initialSecondCardBalance + 1;

        var transferPage = dashboardPage.selectCardForTopUp(firstCard);
        dashboardPage = transferPage.transferFrom(
                secondCard.getCardNumber(),
                excessiveAmount
        );

        int actualFirstCardBalance = dashboardPage.getCardBalance(firstCard);
        int actualSecondCardBalance = dashboardPage.getCardBalance(secondCard);

        assertEquals(initialFirstCardBalance, actualFirstCardBalance,
                "Баланс первой карты не должен измениться при переводе суммы превышающей баланс");
        assertEquals(initialSecondCardBalance, actualSecondCardBalance,
                "Баланс второй карты не должен измениться при переводе суммы превышающей баланс");
    }
}