package com.rockthevote.grommet.data.db.model;

import androidx.annotation.NonNull;

public enum Gender {
    MALE("male"), FEMALE("female");

    private final String gender;

    Gender(String gender) {
        this.gender = gender;
    }

    @Override
    @NonNull
    public String toString() {
        return gender;
    }

    @NonNull
    public static Gender fromString(String gender) {
        for (Gender value : Gender.values()) {
            if (value.gender.equals(gender)) {
                return value;
            }
        }
        return MALE;
    }

    @NonNull
    public static Gender fromPrefix(Prefix prefix) {
        switch (prefix) {
            case MS:
            case MRS:
            case MISS:
                return FEMALE;
            case MR:
            default:
                return MALE;
        }
    }
}
