package com.rockthevote.grommet.data.db.model;

import java.util.Locale;

public enum Race {
    DECLINE("DECLINE TO STATE", "RECHAZAR"),
    ASIAN("ASIAN", "ASIÁTICO"),
    BLACK("BLACK OR AFRICAN AMERICAN", "AFROAMERICANO"),
    HISPANIC_OR_LATINO("HISPANIC OR LATINO", "HISPÁNO/A O LATINO/A"),
    AM_IND_AK_NATIVE("NATIVE AMERICAN OR ALASKAN NATIVE", "NATIVO AMERICANO/A O NATIVO/A DE ALASKA"),
    NATIVE_HAWAIIAN("NATIVE HAWAIIAN OR OTHER PACIFIC ISLANDER","HAWAIANO/A O ISLEÑO/A DEL PACÍFICO"),
    OTHER("OTHER", "OTRA RAZA"),
    MULTI_RACIAL("TWO OR MORE RACES", "DOS O MÁS RAZAS"),
    WHITE("WHITE", "ANGLOSAJÓN");

    private final String enRace;
    private final String esRace;

    Race(String enRace, String esRace) {
        this.enRace = enRace;
        this.esRace = esRace;
    }

    @Override
    public String toString() {
        if ("es".equals(Locale.getDefault().getLanguage())) {
            return esRace;
        } else {
            // default to english
            return enRace;
        }
    }

    public static Race fromString(String race) {
        for (Race val : values()) {
            if (val.enRace.equals(race) ||
                    val.esRace.equals(race)) {
                return val;
            }
        }
        return DECLINE;
    }
}
