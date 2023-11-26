package com.example.weatherapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.SearchView
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//96b5070978082a27d7bf90c55cbb2aff
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("jaipur")
        searchCity()

    }

    private fun searchCity() {
        val searchView=binding.searchView
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit=Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response=retrofit.getWeatherData(cityName,"96b5070978082a27d7bf90c55cbb2aff","metric")
        response.enqueue(object :Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody=response.body()
                if (response.isSuccessful && responseBody!=null){
                    val temperature=responseBody.main.temp.toString()
                    val humidity=responseBody.main.humidity
                    val windSpeed=responseBody.wind.speed
                    val sunRise=responseBody.sys.sunrise.toLong()
                    val sunSet=responseBody.sys.sunset.toLong()
                    val seaLevel=responseBody.main.pressure
                    val condition=responseBody.weather.firstOrNull()?.main?: "unknown"
                    val maxTemp=responseBody.main.temp_max
                    val minTemp=responseBody.main.temp_min
                    val isNight = isNight(sunRise, sunSet)
                    binding.temp.text="$temperature °C "
                    binding.weather.text=condition
                    binding.maxTem.text="Max Temp: $maxTemp°C"
                    binding.minTem.text="Min Temp: $minTemp°C"
                    binding.humidity.text="$humidity %"
                    binding.windspeed.text="$windSpeed m/s"
                    binding.sunrise.text="${time(sunRise)}"
                    binding.sunset.text="${time(sunSet)}"
                    binding.sea.text="$seaLevel hPa"
                    binding.condition.text=condition
                    binding.day.text=dayName(System.currentTimeMillis())
                    binding.date.text=date()
                    binding.cityName.text="$cityName"
                    if (isNight) {
                        // Set night background and night animation
                        binding.root.setBackgroundResource(R.drawable.night_background)
                        //binding.lottieAnimationView.setAnimation(R.raw.night_animation)
                    }

                    //Log.d("TAG", "onResponse: $temperature")
                    changeImageAccordingToWeatherCondition(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }


        })
    }

    private fun changeImageAccordingToWeatherCondition(conditions: String) {
        val currentTime = System.currentTimeMillis() / 1000
        when(conditions){




            "Haze","Partly Clouds","Clouds","Overcast","Mist","Foggy"->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky","Sunny","Clear"->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Rain","Light Rain","Drizzle","Moderate Rain","Showers","Heavy Rain"->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow","Moderate Snow","Heavy Snow","Blizzard","Snow"->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)

            }
        }

        binding.lottieAnimationView.playAnimation()


    }

    fun dayName(timestamp: Long):String{
        val sdf=SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())

    }fun time(timestamp: Long):String{
        val sdf=SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp*1000))

    }
    fun date():String{
        val sdf=SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())

    }
    private fun isNight(sunriseTimestamp: Long, sunsetTimestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis() / 1000 // Convert to seconds
        return currentTime < sunriseTimestamp || currentTime > sunsetTimestamp
    }


}