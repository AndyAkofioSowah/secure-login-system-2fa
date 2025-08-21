package com.securelogin.util;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

public final class OtpUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base32 BASE32 = new Base32();

    // TOTP params (Google Authenticator defaults)
    private static final int    DIGITS = 6;
    private static final int    TIME_STEP_SECONDS = 30;
    private static final int    ALLOWED_DRIFT_STEPS = 1; // ±1 step (30s) for clock skew

    private OtpUtils() { /* no-op */ }

    /** 160-bit random secret, Base32-encoded (compatible with GA, Authy, etc). */
    public static String generateBase32Secret() {
        byte[] bytes = new byte[20]; // 160 bits
        RANDOM.nextBytes(bytes);
        return BASE32.encodeToString(bytes);
    }

    /** otpauth:// URI for QR codes. */
    public static String generateUri(String issuer, String accountName, String base32Secret) {
        String encIssuer  = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encIssuer, encAccount, base32Secret, encIssuer
        );
    }

    /** Validate a 6-digit TOTP code against the given Base32 secret. */
    public static boolean isCodeValid(String base32Secret, String code) {
        if (base32Secret == null || code == null) return false;

        String candidate = code.replaceAll("\\s", "");
        if (!candidate.matches("\\d{" + DIGITS + "}")) return false;

        byte[] key = BASE32.decode(base32Secret);
        if (key == null || key.length == 0) return false;

        long nowStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;

        // Check current step and small window for clock skew
        for (int i = -ALLOWED_DRIFT_STEPS; i <= ALLOWED_DRIFT_STEPS; i++) {
            int totp = oneTimePassword(key, nowStep + i);
            String valid = String.format("%0" + DIGITS + "d", totp);
            if (valid.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    // --- Helpers ---

    private static int oneTimePassword(byte[] key, long timeStep) {
        try {
            byte[] counter = ByteBuffer.allocate(8).putLong(timeStep).array();
            Mac mac = Mac.getInstance("HmacSHA1"); // TOTP = HOTP(HMAC-SHA1) over time counter
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hmac = mac.doFinal(counter);

            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary = ((hmac[offset]     & 0x7f) << 24) |
                    ((hmac[offset + 1] & 0xff) << 16) |
                    ((hmac[offset + 2] & 0xff) <<  8) |
                    ( hmac[offset + 3] & 0xff);

            return binary % (int) Math.pow(10, DIGITS);
        } catch (Exception e) {
            return -1; // treat as invalid
        }
    }

    /** Optional: handy for local debugging (don’t use in production logs). */
    public static String currentCodeFor(String base32Secret) {
        byte[] key = BASE32.decode(base32Secret);
        long step = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        int totp = oneTimePassword(key, step);
        return String.format("%0" + DIGITS + "d", totp);
    }
}
