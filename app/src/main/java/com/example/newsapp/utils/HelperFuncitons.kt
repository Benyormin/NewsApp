package com.example.newsapp.utils

import android.text.format.DateUtils
import android.util.Log
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.apache.commons.net.ntp.NTPUDPClient
import java.util.Date


class HelperFuncitons {

    companion object {

        fun getRelativeTimeAndroid(publishedAt: String): String {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            return try {
                val publishedTime = isoFormat.parse(publishedAt)?.time ?: return "Unknown"
                val networkTimeMillis = getNetworkTime() ?: System.currentTimeMillis()

                DateUtils.getRelativeTimeSpanString(
                    publishedTime,
                    networkTimeMillis,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString()
            } catch (e: Exception) {
                Log.e("HelperFunctions", "Failed to parse date: $publishedAt", e)
                "Invalid date"
            }
        }

        //TODO: make a better network request, Fix bug for invalid dates.
        fun getNetworkTime(): Long? {
            return try {
                val timeClient = NTPUDPClient()
                timeClient.defaultTimeout = 5000 // 5 seconds timeout
                val inetAddress = InetAddress.getByName("time.google.com") // Use Google's NTP server
                val timeInfo = timeClient.getTime(inetAddress)
                val networkTime = timeInfo.message.transmitTimeStamp.time // Returns current UTC time in milliseconds
                timeClient.close()
                networkTime
            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null if fetching fails
            }
        }








    }
}