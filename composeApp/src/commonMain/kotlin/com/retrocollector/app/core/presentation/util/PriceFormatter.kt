package com.retrocollector.app.core.presentation.util

object PriceFormatter {

    /**
     * Formats a floating-point price into a clean currency string with 2 decimal places.
     * Examples:
     * - 35.0 -> "CHF 35.00"
     * - 12.5 -> "CHF 12.50"
     * - null -> "CHF --" (or "—" when includeCurrency is false)
     */
    fun format(
        amount: Double?,
        currency: String = "CHF",
        includeCurrency: Boolean = true,
        placeholder: String = "--"
    ): String {
        if (amount == null) {
            return if (includeCurrency) "$currency $placeholder" else "—"
        }
        val rounded = (amount * 100).toLong()
        val whole = rounded / 100
        val fraction = rounded % 100
        val fracStr = if (fraction < 10) "0$fraction" else "$fraction"
        val formattedNumber = "$whole.$fracStr"

        return if (includeCurrency) "$currency $formattedNumber" else formattedNumber
    }

    /**
     * Formats only the numerical amount with 2 decimal places, omitting currency.
     */
    fun formatAmount(amount: Double): String = format(amount, includeCurrency = false)
}
