package ru.netology.web.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ru.netology.web.data.DataHelper.CardInfo;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class DashboardPage {
    private SelenideElement heading = $("[data-test-id=dashboard]");
    private ElementsCollection cards = $$(".list__item");

    private static final String BALANCE_START = "баланс: ";
    private static final String BALANCE_END = " р.";

    public DashboardPage() {
        heading.shouldBe(visible);
    }

    public int getCardBalance(CardInfo cardInfo) {
        SelenideElement card = cards.findBy(text(cardInfo.getMaskedNumber()));
        return extractBalance(card.text());
    }

    public TransferPage selectCardForTopUp(CardInfo cardInfo) {
        SelenideElement card = cards.findBy(text(cardInfo.getMaskedNumber()));
        card.$("[data-test-id=action-deposit]").click();
        return new TransferPage();
    }

    private int extractBalance(String text) {
        int start = text.indexOf(BALANCE_START);
        int end = text.indexOf(BALANCE_END);
        String value = text.substring(start + BALANCE_START.length(), end).trim();
        return Integer.parseInt(value);
    }
}