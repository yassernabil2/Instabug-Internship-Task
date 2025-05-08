package com.example.instabug_weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instabug_weatherapp.databinding.ItemForecastBinding

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    private val weatherList = mutableListOf<WeatherData>()

    fun updateData(newList: List<WeatherData>) {
        weatherList.clear()
        weatherList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastAdapter.ForecastViewHolder, position: Int) {
        holder.bind(weatherList[position])
    }

    override fun getItemCount(): Int = weatherList.size

    class ForecastViewHolder(private val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(weatherData: WeatherData) {
            binding.itemDate.text = weatherData.date
            binding.itemCondition.text = weatherData.condition
            binding.itemTemp.text = "${weatherData.temperature.toInt()}°"
            binding.itemIc.setImageResource(getIconResource(weatherData.icon))
        }

        private fun getIconResource(icon: String): Int {
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
}