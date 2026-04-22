package com.ptit.tour.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9-]");

    private SlugUtils() {}

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return NON_SLUG.matcher(
            WHITESPACE.matcher(
                NON_ASCII.matcher(normalized).replaceAll("")
                    .toLowerCase(Locale.ROOT)
            ).replaceAll("-")
        ).replaceAll("");
    }
}
