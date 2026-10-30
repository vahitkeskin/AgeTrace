package com.vahitkeskin.agetrace.data.platform

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppContextHolder {
    var context: Context? = null
}
