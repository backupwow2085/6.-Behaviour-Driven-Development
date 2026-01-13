package ru.netology.web.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class TransferPage {

    private SelenideElement heading = $("[data-test-id=dashboard]");
    private SelenideElement amountInput = $("[data-test-id=amount] input");
    private SelenideElement fromInput = $("[data-test-id=from] input");
    private SelenideElement transferButton = $("[data-test-id=action-transfer]");
    private SelenideElement cancelButton = $("[data-test-id=action-cancel]");

    public TransferPage() {
        heading.shouldBe(visible);
    }

    public TransferPage fillAmount(int amount) {
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    public TransferPage fillFrom(String fromCardNumber) {
        fromInput.setValue(fromCardNumber);
        return this;
    }

    public DashboardPage transfer() {
        transferButton.click();
        return page(DashboardPage.class);
    }

    public DashboardPage transferFrom(String fromCardNumber, int amount) {
        return fillAmount(amount).fillFrom(fromCardNumber).transfer();
    }

    public TransferPage fillForm(String cardNumber, int amount) {
        return fillAmount(amount).fillFrom(cardNumber);
    }
    
    public DashboardPage cancel() {
        cancelButton.click();
        return page(DashboardPage.class);
    }
}
