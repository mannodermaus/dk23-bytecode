package de.mannodermaus.dk23.models

import de.mannodermaus.dk23.Redacted

data class User(
    val name: String,
    val age: Int,
    @Redacted
    val password: String,
    @Redacted
    val luckyNumber: Int,
)
