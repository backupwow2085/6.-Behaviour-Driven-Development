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

    // Добавляем информацию о картах
    public static class CardInfo {
        public static final String FIRST_CARD_NUMBER = "5559 0000 0000 0001";
        public static final String SECOND_CARD_NUMBER = "5559 0000 0000 0002";
        public static final String FIRST_CARD_LAST_DIGITS = "0001";
        public static final String SECOND_CARD_LAST_DIGITS = "0002";
    }
}