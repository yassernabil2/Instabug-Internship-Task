package com.example.instabug_weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.instabug_weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: WeatherViewModel
    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null

    /*
    - LocationListener object to handle location updates
    - Updates the lastLocation variable with the new location
    - Tells the viewModel to get the weather data for the new location by calling the loadWeather and loadForecast functions
     */
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLocation = location
            viewModel.loadWeather(location)
            viewModel.loadForecast(location)

            locationManager.removeUpdates(this)
        }
    }

    /*
    - Handles the requesting of location permission at runtime
    - If permission is granted, it calls the getLocation function to get the user's location
    - If permission is denied, it shows a toast message
     */
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getLocation()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*
        - Initializes the viewModel with the WeatherViewModel class
        - ViewModelProvider is used to get the viewModel instance tied to this activity lifecycle
         */
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        /*
        - Observes the currentWeather LiveData object in the viewModel
        - Updates the UI with the new weather data
         */
        viewModel.currentWeather.observe(this) { weather ->
            if (weather != null) {
                binding.cityTxt.text = weather.city
                binding.tempTxt.text = "${weather.temperature}°C"
                binding.conditionTxt.text = weather.condition
                binding.dateTxt.text = weather.date
                binding.iconImg.setImageResource(getIcons(weather.icon))
            }
        }

        /*
        - Observes the error LiveData object in the viewModel
        - Shows a toast message with the error message
         */
        viewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        /*
        - Handles the click event of the search button
        - Gets the city name from the input field
        - If city is not empty calls the loadWeatherByCity function in the viewModel to get the weather data for the given city
        - If city is empty shows a toast message
        - Hides the keyboard
         */
        binding.searchBtn.setOnClickListener {
            val city = binding.cityInput.text.toString().trim()
            if (city.isNotEmpty()) {
                viewModel.loadWeatherByCity(city)
                (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(
                    binding.cityInput.windowToken, 0
                )
            } else {
                Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show()
            }
            binding.cityInput.clearFocus()
        }

        /*
        - Handles the click event of the forecast button
        - Gets the city name from the input field
        - If city is not empty calls the loadForecastByCity function in the viewModel to get the forecast data for the given city
        - If city is empty and lastLocation is not null calls the loadForecast
        - Starts the ForecastActivity and passes the city name or latitude and longitude as extras
         */
        binding.forecastBtn.setOnClickListener {
            val city = binding.cityInput.text.toString().trim()
            val intent = Intent(this, ForecastActivity::class.java)
            if (city.isNotEmpty()) {
                intent.putExtra("city", city)
            } else if (lastLocation != null) {
                intent.putExtra("lat", lastLocation!!.latitude)
                intent.putExtra("long", lastLocation!!.longitude)
            }
            startActivity(intent)
        }

        /*
        - Handles the click event of the current location button
        - Calls the getLocation function to get the user's location
         */
        binding.detectLocationBtn.setOnClickListener {
            getLocation()
            binding.cityInput.text?.clear()
            binding.cityInput.clearFocus()
        }

        /*
        - Handles the swipe refresh event
        - Gets the city name from the input field
        - If city is not empty calls the loadWeatherByCity function in the viewModel to get the weather data for the given city
        - If city is empty and lastLocation is not null calls the loadWeather and loadForecast functions in the viewModel to get the weather
         */
        binding.swipeRefresh.setOnRefreshListener {
            val city = binding.cityInput.text.toString().trim()
            if (city.isNotEmpty()) {
                viewModel.loadWeatherByCity(city)
            } else {
                lastLocation?.let {
                    viewModel.loadWeather(it)
                    viewModel.loadForecast(it)
                }
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }

    /*
    - LocationManager gets the system location service
    - Checks if the location permission is granted
    - If permission is not granted, it requests the permission using the requestPermissionLauncher
    - If permission is granted, it requests a location update from the GPS provider
     */
    private fun getLocation() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0f, locationListener
            )
        } else Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
    }

    private fun getIcons(icon: String): Int {
        return when (icon) {
            "clear-day" -> R.drawable.ic_clear_day
            "clear-night" -> R.drawable.ic_clear_night
            "partly-cloudy-day" -> R.drawable.ic_partly_cloudy
            "partly-cloudy-night" -> R.drawable.ic_partly_cloudy
            "cloudy" -> R.drawable.ic_cloudy
            "rain" -> R.drawable.ic_rain
            "snow" -> R.drawable.ic_snow
            "fog" -> R.drawable.ic_fog
            else -> R.drawable.ic_unknown
        }
    }
}