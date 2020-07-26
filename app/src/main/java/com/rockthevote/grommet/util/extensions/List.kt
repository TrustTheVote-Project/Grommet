package com.rockthevote.grommet.util.extensions

fun List<String?>.listOfNotNullOrEmpty(): List<String> = mapNotNull {
    if (it.isNullOrEmpty()) {
        null
    } else {
        it
    }
}