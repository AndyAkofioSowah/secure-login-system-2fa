package com.securelogin.util;

import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;

public final class OtpUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base32 BASE32 = new Base32();

    private OtpUtils() { /* no-op */ }

    /**
     * Generates a 160-bit random secret and encodes it Base32 for use in TOTP apps.
     */
    public static String generateBase32Secret() {
        byte[] bytes = new byte[20];              // 160 bits
        RANDOM.nextBytes(bytes);
        return BASE32.encodeToString(bytes);
    }

    /**
     * Builds the otpauth:// URL which most authenticator apps (Google Authenticator,
     * Authy, etc.) can scan to enroll the account.
     */
    public static String buildOtpAuthUrl(
            String issuer,
            String username,
            String base32Secret
    ) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, username, base32Secret, issuer
        );
    }
}
