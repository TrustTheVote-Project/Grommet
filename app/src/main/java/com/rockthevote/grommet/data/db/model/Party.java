package com.rockthevote.grommet.data.db.model;

import java.util.Locale;

public enum Party {
    DEMOCRATIC("Democratic", "Demócrata"),
    REPUBLICAN("Republican", "Republicano"),
    GREEN("Green", "Verde"),
    LIBERTARIAN("Libertarian","Libertario"),
    NO_PARTY("None (No Affiliation)", "Ninguno (Sin afiliación)"),
    OTHER_PARTY("Other", "Otro");

    private final String enParty;
    private final String esParty;

    Party(String enParty, String esParty) {
        this.enParty = enParty;
        this.esParty = esParty;
    }

    @Override
    public String toString() {
        if ("es".equals(Locale.getDefault().getLanguage())) {
            return esParty;
        } else {
            // default to english
            return enParty;
        }
    }

    public static Party fromString(String party) {
        for (Party val : values()) {
            if (val.enParty.equals(party) ||
                    val.esParty.equals(party)) {
                return val;
            }
        }
        return OTHER_PARTY;
    }
}
