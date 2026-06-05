package com.jsingh.XpenseMate.utils

fun getOrdinalSuffix(day: Int): String {
    if (day in 11..13) return "th"
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
