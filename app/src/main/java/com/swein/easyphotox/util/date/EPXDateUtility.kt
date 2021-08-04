package com.swein.easyphotox.util.date

import java.util.*

object EPXDateUtility {
    fun getCurrentDateTimeSSSStringWithNoSpace(connectionString: String): String {
        val calendar: Calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}$connectionString" +
                "${(calendar.get(Calendar.MONTH) + 1)}$connectionString" +
                "${calendar.get(Calendar.DAY_OF_MONTH)}$connectionString" +
                "${calendar.get(Calendar.HOUR_OF_DAY)}$connectionString" +
                "${calendar.get(Calendar.MINUTE)}$connectionString" +
                "${calendar.get(Calendar.SECOND)}$connectionString" +
                "${calendar.get(Calendar.MILLISECOND)}"
    }
}