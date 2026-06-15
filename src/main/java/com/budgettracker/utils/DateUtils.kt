package com.budgettracker.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DB_DATE_FORMAT = "yyyy-MM-dd"
    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    private const val TIME_FORMAT = "HH:mm"

    fun getTodayDbString(): String {
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            .format(Date())
    }

    fun getFirstDayOfMonthDbString(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            .format(cal.time)
    }

    fun dbStringToDisplay(dbDate: String): String {
        return try {
            val date = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault()).parse(dbDate)
            SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            dbDate
        }
    }

    fun calendarToDbString(calendar: Calendar): String {
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            .format(calendar.time)
    }

    fun formatCurrency(amount: Double): String {
        val nf = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return nf.format(amount)
    }

    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun parseTime(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.split(":")
        return if (parts.size == 2) {
            Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
        } else {
            Pair(0, 0)
        }
    }

    fun isEndTimeAfterStartTime(startTime: String, endTime: String): Boolean {
        val (startH, startM) = parseTime(startTime)
        val (endH, endM) = parseTime(endTime)
        val startMinutes = startH * 60 + startM
        val endMinutes = endH * 60 + endM
        return endMinutes >= startMinutes
    }
    fun getDbStringForDaysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault()).format(cal.time)
    }
}
