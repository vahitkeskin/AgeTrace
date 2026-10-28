package com.vahitkeskin.agetrace

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform