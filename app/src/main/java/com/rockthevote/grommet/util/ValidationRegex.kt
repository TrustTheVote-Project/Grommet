package com.rockthevote.grommet.util

object ValidationRegex {
    const val PHONE = "[[:punct:]]*\\d{3}[ [:punct:]]*\\d{3}[ [:punct:]]*\\d{4}\\D*"
    const val ZIP = "\\A\\d{5}(-\\d{4})?\\z"
    const val CITY = "\\A[a-zA-Z0-9#\\-\\s’\\.]*\\z"
    const val ADDRESS = "\\A[a-zA-Z0-9#\\-\\s,\\/\\.]*\\z"
    const val NAME = "\\A[^\\x{1F600}-\\x{1F6FF}]*\\z"
}

object PennValidations {
    const val UNIT_MAX_CHARS = 15
    const val DRIVERS_LICENSE_CHARS = 8
    const val SSN_LAST_4_CHARS = 4
}