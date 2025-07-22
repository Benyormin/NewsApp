// utils/TimeUtils.kt
package com.example.newsapp.utils

object TimeUtils {
    var cachedNetworkTime: Long? = null
    var cacheTimestamp: Long? = null // When we got the cached time

    fun getNow(): Long {
        return if (cachedNetworkTime != null && cacheTimestamp != null) {
            val elapsed = System.currentTimeMillis() - cacheTimestamp!!
            cachedNetworkTime!! + elapsed // approximate real "now"
        } else {
            System.currentTimeMillis()
        }
    }
}
