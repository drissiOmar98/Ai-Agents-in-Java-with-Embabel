package com.omar.blog_agent.util;

/**
 * Small string utility for turning arbitrary titles into URL- and
 * filesystem-safe slugs.
 */
public final class Slugs {

    private Slugs() {
        // Non-instantiable: static helpers only.
    }

    /**
     * Converts a title into a lowercase, hyphen-separated slug suitable for
     * use as both a URL slug and a filename stem.
     *
     * <p>Example: {@code "Spring Boot 4: What's New?"} becomes
     * {@code "spring-boot-4-what-s-new"}.</p>
     *
     * @param title the human-readable title to slugify
     * @return the slugified form, with non-alphanumeric runs collapsed to a
     *         single hyphen and leading/trailing hyphens trimmed
     */
    public static String slugify(String title) {
        return title
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
