package ru.netology.web.data;

import lombok.Value;

public class DataHelper {
    private DataHelper() {
    }

    @Value
    public static class AuthInfo {
        private String login;
        private String password;
    }

    public static AuthInfo getAuthInfo() {
        return new AuthInfo("vasya", "qwerty123");
    }

    @Value
    public static class VerificationCode {
        private String code;
    }

    public static VerificationCode getVerificationCodeFor(AuthInfo authInfo) {
        return new VerificationCode("12345");
    }

    @Value
    public static class CardInfo {
        private String cardNumber;
        private String maskedNumber;
    }

    public static CardInfo getFirstCard() {
        String cardNumber = "5559 0000 0000 0001";
        return new CardInfo(cardNumber, maskCardNumber(cardNumber));
    }

    public static CardInfo getSecondCard() {
        String cardNumber = "5559 0000 0000 0002";
        return new CardInfo(cardNumber, maskCardNumber(cardNumber));
    }

    public static String maskCardNumber(String cardNumber) {
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        if (cleanNumber.length() < 4) {
            return cardNumber;
        }
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}