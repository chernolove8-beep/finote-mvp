package com.chernolove.finote.presentation

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val digitsDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")

fun LocalDate.toInputDate(): String = format(displayDateFormatter)

fun LocalDate.toInputDigits(): String = format(digitsDateFormatter)

fun LocalDate.dayPart(): String = String.format("%02d", dayOfMonth)

fun LocalDate.monthPart(): String = String.format("%02d", monthValue)

fun LocalDate.yearPart(): String = year.toString()

fun buildDateInput(day: String, month: String, year: String): String {
    val normalizedDay = day.filter(Char::isDigit).take(2)
    val normalizedMonth = month.filter(Char::isDigit).take(2)
    val normalizedYear = year.filter(Char::isDigit).take(4)

    return listOf(normalizedDay, normalizedMonth, normalizedYear)
        .filter { it.isNotEmpty() }
        .joinToString(".")
}

fun formatDateInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(8)
    val builder = StringBuilder()

    digits.forEachIndexed { index, char ->
        builder.append(char)
        if ((index == 1 || index == 3) && index != digits.lastIndex) {
            builder.append('.')
        }
    }

    return builder.toString()
}

fun parseInputDate(text: String): LocalDate? {
    val digits = text.filter { it.isDigit() }
    if (digits.length != 8) return null

    val day = digits.substring(0, 2).toIntOrNull() ?: return null
    val month = digits.substring(2, 4).toIntOrNull() ?: return null
    val year = digits.substring(4, 8).toIntOrNull() ?: return null

    return runCatching { LocalDate.of(year, month, day) }.getOrNull()
}
