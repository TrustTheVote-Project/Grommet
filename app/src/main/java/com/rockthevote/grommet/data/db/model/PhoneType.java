package com.rockthevote.grommet.data.db.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public enum PhoneType {
    MOBILE("Mobile", "Móvil"),
    HOME("Home", "Casa"),
    WORK("Work", "Trabajo");

    private final String enPhoneType;
    private final String esPhoneType;

    PhoneType(String enPhoneType, String esPhoneType) {
        this.enPhoneType = enPhoneType;
        this.esPhoneType = esPhoneType;
    }

    @Override
    public @NonNull
    String toString() {
        if ("es".equals(Locale.getDefault().getLanguage())) {
            return esPhoneType;
        } else {
            // default to english
            return enPhoneType;
        }
    }

    @NonNull
    public static PhoneType fromString(String phoneType) {
        for (PhoneType value : PhoneType.values()) {
            if (value.enPhoneType.equals(phoneType) ||
                    value.esPhoneType.equals(phoneType)) {
                return value;
            }
        }
        //use mobile as default
        return MOBILE;
    }
}
