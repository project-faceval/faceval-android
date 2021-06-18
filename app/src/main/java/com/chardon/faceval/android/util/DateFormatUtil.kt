package com.chardon.faceval.android.util

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateFormatUtil {

    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val dateFormat: DateFormat = format

    fun Date.parseISOString(): String {
        return format.format(this)
    }

    fun String.parseDate(): Date {
        return format.parse(this)!!
    }
}