package com.olam.warehouse.presentation.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.olam.warehouse.presentation.utils.Constants.LAST_SYNC
import com.olam.warehouse.presentation.utils.Constants.LAST_SYNCED_TIME_IN_MILLS
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by SangiliPandian C on 18-11-2019.
 */
object DateUtils {
    private const val DATE_FORMAT = "MM/dd/yyyy"
    const val DATE_MONTH_FORMAT = "dd/MM/yyyy"
    private const val NICARAGUA_DATE_FORMAT = "dd/MM/yyyy"
    private const val GHANA_COCOA_DATE_FORMAT = "yyyyMMdd"
    private const val DATE_MONTH_TIME_FORMAT = "dd MMM yyyy HH:mm"
    private const val DATE_MONTH_TIME_FORMAT_AM = "dd/MMM/yyyy HH:mm a"
    private const val UTC = "UTC"
    private const val GMT = "GMT"
     const val NO_OF_DAYS = 90
     const val NO_OF_DAYS_30 = 30

    fun fromMillisToTimeString(millis: Long): String {
        val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return format.format(millis)
    }

    fun convertMillisToTimeString(millis: Long, dateFormat: String): String {
        val format = SimpleDateFormat(dateFormat, Locale.getDefault())
        return format.format(millis)
    }

    fun getCountOfDays(): Long {
        val msDiff = getCurrentTimeInMills() - getLastSyncTime()
        return TimeUnit.MILLISECONDS.toDays(msDiff)
    }

    fun getNoOfDaysStanding(date: String, context: Context): Long? {
        val sdf = SimpleDateFormat("yyyy-MM-dd", LocaleHelper.getLocale(context))
        sdf.timeZone = TimeZone.getTimeZone(UTC)
        val netDate: Date? = sdf.parse(date)
        val date = Date()
        val diff = (netDate?.time?.let { date.time.minus(it) })?.div((1000 * 60 * 60 * 24))
        return diff
    }

    fun getFormatedDate(sdate: String): String {
        if (sdate.isNotEmpty()) {
            val sdf = SimpleDateFormat("yyyyMMdd")
            val newFormat = SimpleDateFormat("dd/MM/yyyy")
            var date = sdf.parse(sdate)
            return newFormat.format(date)
        } else return ""
    }

    fun getFormatedYear(sdate: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val newFormat = SimpleDateFormat("yyyy-MM-dd")
        var date = sdf.parse(sdate)
        return newFormat.format(date)
    }


    fun getFormated(sdate: String): Date {
        val sdf = SimpleDateFormat("yyyyMMdd")
        var date = sdf.parse(sdate)
        return date
    }

    fun getUTCDateTimeCameroonGivenDate(sDate: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val newFormat = SimpleDateFormat("MM/dd/yyyy")
        var date = sdf.parse(sDate)
        return newFormat.format(date)
    }

    fun getCurrentTimeInMills(): Long {
        return Calendar.getInstance().timeInMillis
    }

    fun setLastSyncTime() {
        PreferenceHelper.save(LAST_SYNCED_TIME_IN_MILLS, getCurrentTimeInMills())
    }

    fun getLastSyncTime(): Long {
        return PreferenceHelper.get(LAST_SYNCED_TIME_IN_MILLS, 0L)
    }

    fun setTransLastSyncTime() {
        if (getTransLastSyncTime().toString().isNotEmpty())
            PreferenceHelper.save(Constants.TRANS_LAST_SYNC, getCurrentTimeInMills())
    }

    fun getTransLastSyncTime(): Long {
        return PreferenceHelper.get(Constants.TRANS_LAST_SYNC, 0L)
    }

    fun getDifferenceBtMilliSconds(startTime: Long, endTime: Long): String {
        val duration = endTime.minus(startTime)
        val min = TimeUnit.MILLISECONDS.toSeconds(duration)
        return min.toString().plus(" s")
    }

    fun getUTCDateTime(s: String, context: Context): String {

        val sdf = SimpleDateFormat(NICARAGUA_DATE_FORMAT, LocaleHelper.getLocale(context))
        sdf.timeZone = TimeZone.getTimeZone(UTC)
        val netDate = Date(s.toLong())
        return sdf.format(netDate)
    }

    fun getUTCDateTimeCameroon(s: String, context: Context): String {

        val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(context))
        sdf.timeZone = TimeZone.getTimeZone(UTC)
        val netDate = Date(s.toLong())
        return sdf.format(netDate)
    }

    fun getUTCDateGhanaCocoa(s: String, context: Context): String {
        val sdf = SimpleDateFormat(GHANA_COCOA_DATE_FORMAT, LocaleHelper.getLocale(context))
        sdf.timeZone = TimeZone.getTimeZone(UTC)
        val netDate = Date(s.toLong())
        return sdf.format(netDate)
    }

    fun getUTCDateTimeNicaragua(s: String, context: Context): String {
        /* val sdf = SimpleDateFormat(NICARAGUA_DATE_FORMAT, LocaleHelper.getLocale(context))
         sdf.timeZone = TimeZone.getTimeZone(UTC)
         val netDate = Date(s.toLong())
         return sdf.format(netDate)*/
        /* val calendar = Calendar.getInstance()
         val timeZone = calendar.timeZone
         val sdf = SimpleDateFormat(NICARAGUA_DATE_FORMAT, LocaleHelper.getLocale(context))
         sdf.timeZone = timeZone
         //sdf.timeZone = TimeZone.getTimeZone(timeZone)
         val netDate = Date(s.toLong())
         return sdf.format(netDate)*/

        val formatter = SimpleDateFormat(NICARAGUA_DATE_FORMAT)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = s.toLong()
        return formatter.format(calendar.time)
    }

    fun getUTCDateWithTime(timestamp: String, context: Context): String {
        /*val sdf = SimpleDateFormat(DATE_MONTH_TIME_FORMAT_AM, LocaleHelper.getLocale(context))
        sdf.timeZone = TimeZone.getTimeZone(UTC)
        val netDate = Date(s.toLong())
        return sdf.format(netDate)*/
        val calendar = Calendar.getInstance()
        val timeZone = calendar.timeZone
        val sdf = SimpleDateFormat(DATE_MONTH_TIME_FORMAT_AM, LocaleHelper.getLocale(context))
        sdf.timeZone = timeZone
        //sdf.timeZone = TimeZone.getTimeZone(timeZone)
        val netDate = Date(timestamp.toLong())
        return sdf.format(netDate)
    }

    fun getYesterdayStartTime(): Long {
        val calendar: Calendar = Calendar.getInstance()
        val yesterdayMills = calendar.timeInMillis - 1000L * 60L * 60L * 24L
        //    ConsoleLogger.e(TAG,">>>getYesterdayTime time :: " +yesterdayMills );
        calendar.timeInMillis = yesterdayMills
        calendar[Calendar.MILLISECOND] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.HOUR_OF_DAY] = 0
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
        //    ConsoleLogger.e(TAG,">>>>setCalendar time :: " +sdf.format(calendar.getTime()) );
        return calendar.timeInMillis
    }


    fun getUTCDateTimeMillis(s: String, context: Context): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(context))
        sdf.timeZone = TimeZone.getTimeZone(UTC)
        val netDate: Date? = sdf.parse(s)
        return netDate?.time.toString()
    }

    fun getUTCDateTimeMillisCameroon(s: String, context: Context): String {
        val sdf = SimpleDateFormat(NICARAGUA_DATE_FORMAT, LocaleHelper.getLocale(context))
        sdf.timeZone = TimeZone.getTimeZone(UTC)
        val netDate: Date? = sdf.parse(s)
        return netDate?.time.toString()
    }

    fun setLastInventorySyncTime(timestamp: String?, applicationContext: Context) {
        if (timestamp?.isEmpty()!!)
            PreferenceHelper.save(LAST_SYNC, getCurrentTimeInMills())
        else
            PreferenceHelper.save(LAST_SYNC, getUTCDateTimeMillis(timestamp, applicationContext))
    }

    fun getLastInventorySyncTime(context: Context, timestamp: String): String {
        try {
            val calendar = Calendar.getInstance()
            val timeZone = calendar.timeZone
            val sdf = SimpleDateFormat(DATE_MONTH_TIME_FORMAT, LocaleHelper.getLocale(context))
            sdf.timeZone = timeZone
            //sdf.timeZone = TimeZone.getTimeZone(timeZone)
            val netDate = Date(timestamp.toLong())
            return sdf.format(netDate)
        } catch (e: NumberFormatException) {
            return ""
        }
    }

    fun getTimeStamp(date: String): Long {
        val date = SimpleDateFormat("yyyyMMdd").parse(date)
        /* val calendar = Calendar.getInstance().set(date.substring(0, 4).toInt(), date.substring(4, 6).toInt(), date.substring(6).toInt())
         val l = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             LocalDate.parse(date, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
         } else {
             TODO("VERSION.SDK_INT < O")
         }
         val unix = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             l.atStartOfDay(TimeZone.getTimeZone(UTC).toZoneId()).toInstant().epochSecond
         } else {
             TODO("VERSION.SDK_INT < O")
         }*/

        return date.time
    }

    fun getTimeStampinMilliseconds(date: String): Long {
        val date = SimpleDateFormat("yyyyMMdd").parse(date)
        return date.time
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }
    // Print on Friday 26th August 2022 at 23:23

    fun getCurrentDataForPrint(): String {
        val simpleDateFormat = SimpleDateFormat("EEEE dd MMM yyyy at HH:mm")
        return simpleDateFormat.format(Date())
    }

    fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(Date())
    }

    fun getCurrentDateForNotification(): String {
        val simpleDateFormat = SimpleDateFormat("dd MMM HH:mm")
        return simpleDateFormat.format(Date())
    }


    fun getDate(): String {
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy")
        return simpleDateFormat.format(Date())
    }

    fun getCurrentDateYearFormat(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(Date())
    }


    fun getFormattedCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
        return simpleDateFormat.format(Date())
    }

    fun compareLastSyncTime(context: Context): Boolean {
        //dd/MMM/yyyy HH:mm tt
        val timestamp = PreferenceHelper.get(Constants.LAST_SYNC_TIME, "")
        if (timestamp.isNotEmpty()) {
            val lastsync = getUTCDateWithTime(timestamp, context)
            val currentTime = getUTCDateWithTime(getCurrentTimeInMills().toString(), context)
            val lastSyncHour = lastsync.split(" ")[1].split(":")[0]
            val currentHour = currentTime.split(" ")[1].split(":")[0]

            val lastNoon = lastsync.split(" ")[2]
            val currentNoon = currentTime.split(" ")[2]

            val dayLast = lastsync.split("/")[0]
            val monthLast = lastsync.split("/")[1]
            val daycurrent = currentTime.split("/")[0]
            val monthCurrent = currentTime.split("/")[1]
            if (!monthLast.equals(monthCurrent)) {
                when {
                    currentHour.toInt() < 7 -> return true
                    else -> return false
                }
            } else if (dayLast < daycurrent) {
                when {
                    currentHour.toInt() < 7 -> return true
                    else -> return false
                }
            } else if (dayLast.equals(daycurrent)) {
                if (lastNoon.equals("PM", true)) return true
                else if (lastSyncHour.toInt() > 7) return true
                else return !(lastSyncHour.toInt() < 7 && currentHour.toInt() > 7)
            } else return false
        } else return true
    }

    fun isFirstDayOfMonth(context: Context, count: Int): Boolean {
        // "dd/MMM/yyyy HH:mm a"
        val currentTime = getUTCDateWithTime(getCurrentTimeInMills().toString(), context)

        val currentHour = currentTime.split(" ")[1].split(":")[0]

        val currentDate = currentTime.split(" ")[0].split("/")[0]

        val lastNoon = currentTime.split(" ")[2]
        if (currentDate.toInt() <= count) {
            if (lastNoon.equals("PM", true) && (currentDate.toInt() == count)) {
                if (android.text.format.DateFormat.is24HourFormat(context)) {
                    if (currentHour.toInt() >= 18) {
                        return true
                    }
                } else {
                    if (currentHour.toInt() >= 6) {
                        return true
                    }
                }
            }
            return false
        }
        /*if (currentDate.equals("01") || currentDate.equals("1") || currentDate.equals("02") || currentDate.equals("2")) {
            if (lastNoon.equals("PM", true) && (currentDate.equals("02") || currentDate.equals("2"))) {
                if (android.text.format.DateFormat.is24HourFormat(context)) {
                    if (currentHour.toInt() >= 18) {
                        return true
                    }
                } else {
                    if (currentHour.toInt() >= 6) {
                        return true
                    }
                }
            }
            return false
        }*/

        return true
    }

    fun addingDaysToCurrentDate(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)

        val formatter = SimpleDateFormat("dd-MMM-yyyy")
        return formatter.format(calendar.time)
    }

    fun addingDaysToDate(date: String, days: Int): String {
        val date1 = SimpleDateFormat("MM/dd/yyyy").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = date1
        calendar.add(Calendar.DAY_OF_YEAR, days)

        val formatter = SimpleDateFormat("MM/dd/yyyy")
        return formatter.format(calendar.time)
    }

    fun subtractingDaysToDate(date: String, days: Int): String {
        val date1 = SimpleDateFormat("MM/dd/yyyy").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = date1
        calendar.add(Calendar.DAY_OF_YEAR, -(days))

        val formatter = SimpleDateFormat("MM/dd/yyyy")
        return formatter.format(calendar.time)
    }

    fun formatDate(date: String): String {
        val date1 = SimpleDateFormat("MM/dd/yyyy").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = date1

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return formatter.format(calendar.time)
    }

    fun formatDateGhana(date: String): String {
        val date1 = SimpleDateFormat("yyyyMMdd").parse(date)
        val formatter = SimpleDateFormat("MM/dd/yyyy")
        return formatter.format(date1)
    }

    fun formatDateGHCocoa(date: String): String {
        if (date.isNotEmpty()) {
            val date1 = SimpleDateFormat("MM/dd/yyyy").parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = date1

            val formatter = SimpleDateFormat("yyyyMMdd")
            return formatter.format(calendar.time)
        } else return ""
    }


    fun formatDateGHCashew(date: String): String {
        val date1 = SimpleDateFormat("MM/dd/yyyy").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = date1

        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.format(calendar.time)
    }


    fun getReadableCurrentDate() = fromMillisToTimeString(getCurrentTimeInMills())

    fun isTimeBetween(start: Int, end: Int): Boolean {
        val date = Calendar.getInstance()
        val hour = date.get(Calendar.HOUR_OF_DAY)
        return hour in start..end
    }

    fun getWeekEndsBetweenTwoDates(startDate: Date?, endDate: Date?): Int {
        val startCal = Calendar.getInstance()
        startCal.time = startDate
        val endCal = Calendar.getInstance()
        endCal.time = endDate
        var weekEnds = 0

        //Return 0 if start and end are the same
        if (startCal.timeInMillis === endCal.timeInMillis) {
            return 0
        }
        while (startCal.timeInMillis <= endCal.timeInMillis) {

            if (startCal[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY || startCal[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
                ++weekEnds
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekEnds
    }

    fun isFirstDaysOfMonth(context: Context, count: Int): Boolean {
        val from = Calendar.getInstance()
        from.set(Calendar.DAY_OF_MONTH, 1)

        val to = Calendar.getInstance()
        to.set(Calendar.DAY_OF_MONTH, count)


        var days = count + getWeekEndsBetweenTwoDates(from.time, to.time)

        val lastDay = Calendar.getInstance()
        lastDay.set(Calendar.DAY_OF_MONTH, days)

        if (lastDay[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
            ++days
        }
        if (lastDay[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY) {
            days = days + 2
        }

        val currentTime = getUTCDateWithTime(getCurrentTimeInMills().toString(), context)

        val currentHour = currentTime.split(" ")[1].split(":")[0]

        val currentDate = currentTime.split(" ")[0].split("/")[0]


        return currentDate.toInt() <= days
    }

    fun oneFormatToOtherFormat(date: String, originalFormat: String, targetFormat: String): String {
        val originalFormat: DateFormat = SimpleDateFormat(originalFormat, Locale.ENGLISH)
        val targetFormat: DateFormat = SimpleDateFormat(targetFormat)
        val date: Date = originalFormat.parse(date)
        return targetFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDateToSpanish(date: String): String {
        var dateInSpanish: String = ""
        if (!date.isNullOrEmpty()) {
            var separated: Array<String>
            if (date.contains("-")) {
                separated = date.toString().split("-").toTypedArray()

            } else {
                var datestringformated = getFormatedDate(date.toString())
                separated = datestringformated.split("/").toTypedArray()

            }
            val localDate: LocalDate =
                LocalDate.of(
                    separated[2].trim().toInt(),
                    separated[1].trim().toInt(),
                    separated[0].trim().toInt()
                )
            val spanishLocale = Locale("es", "ES")
            dateInSpanish =
                localDate.format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy", spanishLocale))
        }
        return dateInSpanish
    }

    fun parseDate(date: String): String {
        var year = date.take(4).plus("/")
        var month = date.takeLast(4)
        var date = year.plus(month.take(2)).plus("/").plus(month.takeLast(2))
        return date
    }

    fun isLastFiveDaysRecord(savedTime: Long): Boolean {
        val msDiff = getCurrentTimeInMills() - savedTime
        return msDiff <= 5 * 24 * 60 * 60 * 1000
    }

    fun convertStringToCalendar(timeMillis: Long): String {
        //get calendar instance
        val calendarDate = Calendar.getInstance()
        calendarDate.timeInMillis = timeMillis
        val month = getAbbreviatedFromDateTime(calendarDate, "MMMM") //prints August
        val day = getAbbreviatedFromDateTime(calendarDate, "EEEE") // prints Wednesday
        val date = getAbbreviatedFromDateTime(calendarDate, "dd") // prints 19
        return day.plus(" ").plus(month).plus(" ").plus(date)
    }


    private fun getAbbreviatedFromDateTime(dateTime: Calendar, field: String): String? {
        val output = SimpleDateFormat(field)
        try {
            return output.format(dateTime.time)    // format output
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun getcurdateformat(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(Date())
    }

    fun getDateString(erDate: String): String {
        var date = ""
        date = if (erDate.length == 8) {
            val milliseconds = getTimeStampinMilliseconds(erDate)
            convertMillisToTimeString(milliseconds, DATE_MONTH_TIME_FORMAT)
        } else {
            convertMillisToTimeString(erDate.toLong(), DATE_MONTH_TIME_FORMAT)
        }
        return date

    }

    /*The below method it to get, days count bewtween data range*/
    fun getDaysCountBwTwoDates(fromDate: String, toDate: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, fromDate.split("-")[0].trim().toInt())
        calendar.set(Calendar.MONTH, fromDate.split("-")[1].trim().toInt())
        calendar.set(Calendar.YEAR, fromDate.split("-")[2].trim().toInt())
        val firstDate: Date = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, toDate.split("-")[0].trim().toInt())
        calendar.set(Calendar.MONTH, toDate.split("-")[1].trim().toInt())
        calendar.set(Calendar.YEAR, toDate.split("-")[2].trim().toInt())
        val secondDate: Date = calendar.time
        val diff = secondDate.time - firstDate.time

        /*converting milliseconds to days*/
        return (diff / 1000 / 60 / 60 / 24) + 1/*the +1 is to add current date also*/
    }

    public fun getAllDatesBwTwoDates(fromDate: String, toDate: String): ArrayList<String> {
        var dateList = ArrayList<Date>()
        var dateStringList = ArrayList<String>()
        val format = SimpleDateFormat("dd-MM-yyyy")
        var date1 = format.parse(fromDate)
        var date2 = format.parse(toDate)
        var cal1 = Calendar.getInstance()
        cal1.time = date1
        var cal2 = Calendar.getInstance()
        cal2.time = date2
        while (!cal1.after(cal2)) {
            dateList.add(cal1.time)
            cal1.add(Calendar.DATE, 1);
        }
        for (item in dateList) {
//            var dat = Date(item.time)
//            var forma = format.format(datee)
            dateStringList.add(format.format(Date(item.time)))
//            dateStringList.add(item.toString().split(" ")[1].plus(" ").plus(item.toString().split(" ")[2]))
        }
        return dateStringList
    }
}
