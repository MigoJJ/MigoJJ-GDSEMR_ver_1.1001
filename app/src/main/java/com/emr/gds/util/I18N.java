package com.emr.gds.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {

    private static final Logger logger = LoggerFactory.getLogger(I18N.class);
    private static final String BUNDLE_BASE_NAME = "messages";
    private static ResourceBundle bundle;

    static {
        loadBundle(Locale.getDefault());
    }

    private static void loadBundle(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
        } catch (MissingResourceException e) {
            logger.warn("리소스 번들을 찾을 수 없음: locale={}, 기본 locale로 대체", locale);
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.ROOT);
        }
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            logger.warn("리소스 키 누락: key='{}', locale={}", key, bundle.getLocale());
            return "!!" + key + "!!";
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
