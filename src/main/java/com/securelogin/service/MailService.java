package com.securelogin.service;

public interface MailService {
    void send(String to, String subject, String body);

    // Convenience method used by ForgotPasswordController
    default void sendPasswordReset(String to, String resetUrl, int minutes) {
        String subject = "Reset your password";
        String body = """
                Hi,

                Someone requested a password reset for your account.
                If this was you, click the link below (valid for %d minutes):

                %s

                If you didn't request it, you can ignore this email.
                """.formatted(minutes, resetUrl);

        send(to, subject, body);
    }
}


