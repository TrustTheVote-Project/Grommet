package com.rockthevote.grommet.util

object ValidationRegex {
    const val PHONE = "^\$|^[[:punct:]]*\\d{3}[ [:punct:]]*\\d{3}[ [:punct:]]*\\d{4}\\D*"
    const val ZIP = "\\A\\d{5}(-\\d{4})?\\z"
    const val CITY = "\\A[a-zA-Z0-9#\\-\\s’\\.]*\\z"
    const val ADDRESS = "\\A[a-zA-Z0-9#\\-\\s,\\/\\.]*\\z"
    const val NAME = "\\A[^\\x{1F600}-\\x{1F6FF}]*\\z"
    const val EMAIL = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
}

object PennValidations {
    const val UNIT_MAX_CHARS = 15
    const val DRIVERS_LICENSE_CHARS = 8
    const val SSN_LAST_4_CHARS = 4
}