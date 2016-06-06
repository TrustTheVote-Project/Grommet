package com.rockthevote.grommet.data.api.model;

import android.support.annotation.NonNull;

public enum PhoneType {
    MOBILE("Mobile"), HOME("Home"), WORK("Work");

    private final String phoneType;

    PhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    @Override
    public String toString() {
        return phoneType;
    }

    @NonNull
    public static PhoneType fromString(String phoneType) {
        for (PhoneType value : values()) {
            if (value.phoneType.equals(phoneType)) {
                return value;
            }
        }
        //use mobile as default
        return MOBILE;
    }
}
