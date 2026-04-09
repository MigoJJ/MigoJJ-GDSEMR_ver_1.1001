package com.emr.gds.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class I18N {
    private static final String BUNDLE_BASE_NAME = "messages";
    private static ResourceBundle bundle;

    // Static initializer to load bundle based on default locale
    static {
        loadBundle(Locale.getDefault());
    }

    private static void loadBundle(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
        } catch (MissingResourceException e) {
            System.err.println("Warning: Resource bundle for locale " + locale + " not found. Using default locale.");
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.ROOT); // Fallback to default
        }
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("Warning: Missing resource key '" + key + "' for locale " + bundle.getLocale());
            return "!!" + key + "!!"; // Indicate missing key
        }
    }

    public static void setLocale(Locale newLocale) {
        if (!bundle.getLocale().equals(newLocale)) {
            loadBundle(newLocale);
        }
    }
    
    public static Locale getLocale() {
        return bundle.getLocale();
    }
}
