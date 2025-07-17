package com.arny.aiprompts.utils

import kotlinx.datetime.*
import java.util.Date

/**
 * Converts a java.util.Date to a kotlinx.datetime.LocalDateTime.
 */
fun Date.toKotlinxLocalDateTime(): LocalDateTime {
    // 1. Convert the java.util.Date to a kotlinx.datetime.Instant
    //    by using the epoch milliseconds value.
    val ktxInstant = Instant.fromEpochMilliseconds(this.time)

    // 2. Convert the Instant to a LocalDateTime using the system's
    //    default time zone. The 'currentSystemDefault()' function
    //    is what requires the kotlinx.datetime.TimeZone import.
    return ktxInstant.toLocalDateTime(TimeZone.currentSystemDefault())
}

/**
 * Converts a kotlinx.datetime.LocalDateTime back to a java.util.Date.
 */
fun LocalDateTime.toJavaDate(): Date {
    // This conversion also requires a time zone to correctly
    // convert the local date-time to a specific moment in time (Instant).
    val ktxInstant = this.toInstant(TimeZone.currentSystemDefault())
    return Date(ktxInstant.toEpochMilliseconds())
}
