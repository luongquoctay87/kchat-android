package com.kchat.data.network.util

import java.util.TimeZone

fun currentUtcOffsetMinutes(): Int =
    TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60_000
