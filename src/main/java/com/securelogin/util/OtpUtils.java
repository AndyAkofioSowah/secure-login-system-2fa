package com.securelogin.util;

import org.apache.commons.codec.binary.Base32;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public static String generateUri(String issuer, String accountName, String base32Secret) {
        String encIssuer  = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encIssuer, encAccount, base32Secret, encIssuer
        );
    }

}
