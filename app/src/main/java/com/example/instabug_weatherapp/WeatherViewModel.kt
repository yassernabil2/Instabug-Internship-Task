package com.example.instabug_weatherapp

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.concurrent.thread

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application)

    /*
     - LiveData object holding the current weather data
     - Expose the LiveData object as read-only
     */
    private val _currentWeather = MutableLiveData<WeatherData>()
    val currentWeather: LiveData<WeatherData> = _currentWeather

    /*
     - LiveData object holding the forecast data
     - Expose the LiveData object as read-only
     */
    private val _forecast = MutableLiveData<List<WeatherData>>()
    val forecast: LiveData<List<WeatherData>> = _forecast

    /*
     - LiveData object holding error messages
     - Expose the LiveData object as read-only
     */
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /*
    - Loads the current weather data for the given location
    - Runs the code with separate thread in background to avoid blocking the UI thread
    - Calls the repository to get the current weather for given location
    - Updates the LiveData object with the new weather data
     */
    fun loadWeather(location: Location) {
        thread {
            if (!repository.isOnline()) {
                _error.postValue("No internet connection")
            } else {
                _error.postValue(null)
            }
            val weatherLocation = repository.getCurrentWeather(location.latitude, location.longitude)
            _currentWeather.postValue(weatherLocation)
            if (weatherLocation == null) _error.postValue("Failed to load current weather")
        }
    }

    fun loadWeatherByCity(city: String) {
        thread {
            if (!repository.isOnline()) {
                _error.postValue("No internet connection")
            } else {
                _error.postValue(null)
            }
            val weatherCity = repository.getCurrentWeatherByCity(city)
            _currentWeather.postValue(weatherCity)
            if (weatherCity == null) _error.postValue("Failed to load current weather")
        }
    }

    /*
    - Loads the 5-day forecast data for given location
    - Runs the code with separate thread in background to avoid blocking the UI thread
    - Calls the repository to get the forecast for given location
    - Updates the LiveData object with the new forecast data
     */
    fun loadForecast(location: Location) {
        thread {
            if (!repository.isOnline()) {
                _error.postValue("No internet connection")
            } else {
                _error.postValue(null)
            }
            val forecastList = repository.getForecast(location.latitude, location.longitude)
            _forecast.postValue(forecastList)
            if (forecastList == null) _error.postValue("Failed to load forecast")
        }
    }

    fun loadForecastByCity(city: String) {
        thread {
            if (!repository.isOnline()) {
                _error.postValue("No internet connection")
            } else {
                _error.postValue(null)
            }
            val forecastList = repository.getForecastByCity(city)
            _forecast.postValue(forecastList)
            if (forecastList == null) _error.postValue("Failed to load forecast")
        }
    }
}