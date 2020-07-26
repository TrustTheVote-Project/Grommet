package com.rockthevote.grommet.data.db.model;

public enum FormLanguage {
    ENGLISH("en"),
    SPANISH("es");

    private final String language;

    FormLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return language;
    }

    public static FormLanguage fromString(String language) {
        for (FormLanguage val : values()) {
            if (val.toString().equals(language)) {
                return val;
            }
        }
        return ENGLISH;
    }
}
