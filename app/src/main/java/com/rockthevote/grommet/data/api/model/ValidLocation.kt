package com.rockthevote.grommet.data.api.model

data class ValidLocation(
    val id: Int,
    val name: String
){
    override fun toString(): String {
        return name
    }
}