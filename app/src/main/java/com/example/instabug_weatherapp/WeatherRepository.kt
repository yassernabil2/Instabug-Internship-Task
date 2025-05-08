package com.example.instabug_weatherapp

import android.content.Context
import android.net.ConnectivityManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository(private val context: Context) {
    private val apiKey = "API_KEY"

    /*
    -Boolean fun returns true if the device is connected to the internet
    -ConnectivityManager is used to check the network capabilities
    -ActiveNetwork used to get the active network or false if none
    -getNetworkCapabilities used to get the all capabilities of the active network
    -hasCapability used to check if the network has internet capability
 */
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /*
    - getCurrentWeather fun returns the current weather data for a given latitude and longitude
    - if the device is online, it fetches the data from the API and caches it in shared preferences
    - parses the JSON response into a WeatherData object
    - if the device is offline, it returns the cached data
     */
    fun getCurrentWeather(lat: Double, lon: Double): WeatherData? {
        val urlString =
            "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$lat,$lon/today?unitGroup=metric&key=$apiKey&contentType=json"
        return try {
            val response = httpGet(urlString)
            cacheData("current_weather", response)
            parseCurrentWeather(response)
        } catch (e: Exception) {
            getCachedCurrentWeather()
        }
    }

    fun getCurrentWeatherByCity(city: String): WeatherData? {
        val urlString =
            "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/today?unitGroup=metric&key=$apiKey&contentType=json"
        return try {
            val response = httpGet(urlString)
            cacheData("current_weather", response)
            parseCurrentWeather(response)
        } catch (e: Exception) {
            getCachedCurrentWeather()
        }
    }

    /*
    - getForecast fun returns the forecast weather data for 5-days for a given latitude and longitude
    - if the device is online, it fetches the data from the API and caches it in shared preferences
    - parses the JSON response into a list of WeatherData objects
    - if the device is offline, it returns the cached data
     */
    fun getForecast(lat: Double, lon: Double): List<WeatherData>? {
        val urlString =
            "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$lat,$lon/next5days?unitGroup=metric&key=$apiKey&contentType=json"
        return try {
            val response = httpGet(urlString)
            cacheData("forecast", response)
            parseForecast(response)
        } catch (e: Exception) {
            getCachedForecast()
        }
    }

    fun getForecastByCity(city: String): List<WeatherData>? {
        val urlString =
            "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/next5days?unitGroup=metric&key=$apiKey&contentType=json"
        return try {
            val response = httpGet(urlString)
            cacheData("forecast", response)
            parseForecast(response)
        } catch (e: Exception) {
            getCachedForecast()
        }
    }

    /*
    - Helper fun to make an HTTP GET request and return the response as a string
    - Uses HttpURLConnection to make the request
    - BufferedReader reads the response from the input stream
    - converts the response to a string and returns it
    - close the reader and disconnect the connection after reading the response to avoid resource leaks
     */
    private fun httpGet(urlString: String): String {
        val url = URL(urlString)
        val connectivity = url.openConnection() as HttpURLConnection

        connectivity.requestMethod = "GET"
        connectivity.connectTimeout = 5000
        connectivity.readTimeout = 5000

        val reader = BufferedReader(InputStreamReader(connectivity.inputStream))
        val result = reader.readText()

        reader.close()
        connectivity.disconnect()

        return result
    }

    /*
    - Helper fun to save(cache) the data in shared preferences
     */
    private fun cacheData(key: String, data: String) {
        val sharedPreferences = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, data)
        editor.apply()
    }

    /*
    - Helper fun to load the cached current weather data from shared preferences
     */
    private fun getCachedCurrentWeather(): WeatherData? {
        val sharedPreferences = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
        val data = sharedPreferences.getString("current_weather", null) ?: return null
        return parseCurrentWeather(data)
    }

    /*
    - Helper fun to load the cached forecast weather data from shared preferences
     */
    private fun getCachedForecast(): List<WeatherData>? {
        val sharedPreferences = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
        val data = sharedPreferences.getString("forecast", null) ?: return null
        return parseForecast(data)
    }

    /*
    - Helper fun to parse the JSON response for current weather into a WeatherData object
    - Uses JSONObject to parse the JSON response
    - Gets the days array from the JSON response
    - Gets the first day object from the days array(today's weather)
    - Gets the date, city, temperature, and condition from the first day object
    - Returns a WeatherData object with the parsed data
     */
    private fun parseCurrentWeather(json: String): WeatherData? {
        val jsonObject = JSONObject(json)
        val daysArray = jsonObject.getJSONArray("days")
        if (daysArray.length() == 0) return null
        val today = daysArray.getJSONObject(0)
        return WeatherData(
            date = today.getString("datetime"),
            city = jsonObject.getString("resolvedAddress"),
            temperature = today.getInt("temp"),
            condition = today.getString("conditions"),
            icon = today.getString("icon"),
        )
    }

    /*
    - Helper fun to parse the JSON response for forecast weather into a list of WeatherData objects
    - Uses JSONObject to parse the JSON response
    - Gets the days array from the JSON response
    - Loops through the days array and parses each day object into a WeatherData object
    - Adds the WeatherData object to a list and returns the list
     */
    private fun parseForecast(json: String): List<WeatherData>? {
        val jsonObject = JSONObject(json)
        val daysArray = jsonObject.getJSONArray("days")
        val list = mutableListOf<WeatherData>()
        for (i in 0 until daysArray.length()) {
            val day = daysArray.getJSONObject(i)
            list.add(
                WeatherData(
                    date = day.getString("datetime"),
                    city = jsonObject.getString("resolvedAddress"),
                    temperature = day.getInt("temp"),
                    condition = day.getString("conditions"),
                    icon = day.getString("icon")
                )
            )
        }
        return list
    }
}