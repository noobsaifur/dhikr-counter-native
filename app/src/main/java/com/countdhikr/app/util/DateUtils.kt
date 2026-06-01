package com.countdhikr.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {

    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD

    private val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)

    /**
     * Returns today's date as "YYYY-MM-DD".
     */
    fun getTodayDateString(): String {
        return LocalDate.now().format(isoFormatter)
    }

    /**
     * Formats an ISO date string ("YYYY-MM-DD") into a human-readable form.
     *
     * Example: "2025-05-28" → "Wednesday, 28 May 2025"
     */
    fun formatDisplayDate(dateStr: String): String {
        val date = LocalDate.parse(dateStr, isoFormatter)
        return date.format(displayFormatter)
    }

    /**
     * Calculates the 1-based day number between [startDate] and [currentDate], applying an
     * optional [offset].
     *
     * If [currentDate] is the same as [startDate], the result is `1 + offset`.
     *
     * @param startDate  ISO date string for the start.
     * @param currentDate ISO date string for the current day.
     * @param offset     Additional offset to add (default 0).
     * @return The day number (≥ 1).
     */
    fun calculateDayNumber(startDate: String, currentDate: String, offset: Int = 0): Int {
        val start = LocalDate.parse(startDate, isoFormatter)
        val current = LocalDate.parse(currentDate, isoFormatter)
        val daysBetween = ChronoUnit.DAYS.between(start, current).toInt()
        return maxOf(1, daysBetween + 1 + offset)
    }

    /**
     * Returns a list of ISO date strings for every day from [startDate] to [endDate] inclusive.
     */
    fun getDatesBetween(startDate: String, endDate: String): List<String> {
        val start = LocalDate.parse(startDate, isoFormatter)
        val end = LocalDate.parse(endDate, isoFormatter)
        if (start.isAfter(end)) return emptyList()

        val dates = mutableListOf<String>()
        var current = start
        while (!current.isAfter(end)) {
            dates.add(current.format(isoFormatter))
            current = current.plusDays(1)
        }
        return dates
    }

    /**
     * Formats a 24-hour time string ("HH:mm") into 12-hour ("h:mm a") or keeps 24-hour
     * based on Android system settings.
     */
    fun formatSystemTime(context: android.content.Context, timeStr: String): String {
        try {
            val cleanTime = timeStr.trim().split(" ")[0] // remove timezone info if any
            val parts = cleanTime.split(":")
            if (parts.size < 2) return timeStr
            val hours = parts[0].toIntOrNull() ?: return timeStr
            val minutes = parts[1].toIntOrNull() ?: return timeStr

            val is24 = android.text.format.DateFormat.is24HourFormat(context)
            if (is24) {
                return String.format(Locale.US, "%02d:%02d", hours, minutes)
            } else {
                val amPm = if (hours >= 12) "PM" else "AM"
                val displayHour = when {
                    hours == 0 -> 12
                    hours > 12 -> hours - 12
                    else -> hours
                }
                return String.format(Locale.US, "%d:%02d %s", displayHour, minutes, amPm)
            }
        } catch (e: Exception) {
            return timeStr
        }
    }
}
