package com.example.palamigopos.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val pesoLocale: Locale = Locale.Builder()
        .setLanguage("en")
        .setRegion("PH")
        .build()

    private val pesoFormat: NumberFormat = NumberFormat.getCurrencyInstance(pesoLocale)

    fun format(amount: Double): String = pesoFormat.format(amount)
}
