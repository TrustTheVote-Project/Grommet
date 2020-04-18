package com.rockthevote.grommet.util.extensions

import android.text.Editable

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)