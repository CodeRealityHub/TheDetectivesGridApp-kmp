package com.example.thedetectivesgrid

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform