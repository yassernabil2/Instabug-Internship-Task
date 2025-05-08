package com.example.instabug_weatherapp

import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.instabug_weatherapp.databinding.ActivityForecastBinding

class ForecastActivity : AppCompatActivity() {

    private lateinit var viewModel: WeatherViewModel
    private val forecastAdapter = ForecastAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*
        - Sets the adapter for the forecast recyclerView
         */
        binding.forecastLv.adapter = forecastAdapter

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        /*
        - Observes the forecast LiveData and updates the forecast recyclerView when the forecast changes
         */
        viewModel.forecast.observe(this) { forecast ->
            forecast?.let {
                forecastAdapter.updateData(it)
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        /*
        - Gets the city or location from the intent and loads the forecast accordingly
        - If the city is not null, it loads the forecast by city
        - If the city is null, it loads the forecast by location
         */
        val city = intent.getStringExtra("city")
        if (city != null) {
            viewModel.loadForecastByCity(city)
        } else {
            val location = Location("").apply {
                latitude = intent.getDoubleExtra("lat", 0.0)
                longitude = intent.getDoubleExtra("long", 0.0)
            }
            viewModel.loadForecast(location)
        }
    }
}