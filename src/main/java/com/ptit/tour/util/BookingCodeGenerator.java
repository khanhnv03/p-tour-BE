package com.ptit.tour.util;

import java.security.SecureRandom;

public final class BookingCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private BookingCodeGenerator() {}

    /** Generates a code in the format BK-XXXX (4 random digits). */
    public static String generate() {
        int number = RANDOM.nextInt(9000) + 1000; // 1000–9999
        return "BK-" + number;
    }

    /** Generates an order code in the format ORD-XXXXXXXXXX (10 random digits). */
    public static String generateOrderCode() {
        long number = (long) (RANDOM.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
        return "ORD-" + number;
    }
}
