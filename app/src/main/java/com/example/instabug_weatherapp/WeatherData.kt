package com.example.instabug_weatherapp

data class WeatherData(
    val date: String,
    val city: String,
    val temperature: Int,
    val condition: String,
    val icon: String,
)
