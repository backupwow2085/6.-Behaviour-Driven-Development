package ru.netology.web.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.page;

public class DashboardPage {
    private SelenideElement heading = $("[data-test-id=dashboard]");
    private ElementsCollection cards = $$("[data-test-id]");

    private static final String BALANCE_START = "баланс: ";
    private static final String BALANCE_END = " р.";

    public DashboardPage() {
        heading.shouldBe(visible);
    }

    public int getCardBalance(String lastFourDigits) {
        SelenideElement card = cards.findBy(text(lastFourDigits));
        return extractBalance(card.text());
    }

    public TransferPage selectCardForTopUp(String lastFourDigits) {
        SelenideElement card = cards.findBy(text(lastFourDigits));
        card.$("[data-test-id=action-deposit]").click();
        return page(TransferPage.class);
    }

    private int extractBalance(String text) {
        int start = text.indexOf(BALANCE_START);
        int end = text.indexOf(BALANCE_END);
        String value = text.substring(start + BALANCE_START.length(), end).trim();
        return Integer.parseInt(value);
    }
}