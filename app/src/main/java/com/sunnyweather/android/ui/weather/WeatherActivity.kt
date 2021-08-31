package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastBinding
import com.sunnyweather.android.databinding.LifeIndexBinding
import com.sunnyweather.android.databinding.NowBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    private lateinit var placeName: TextView
    private lateinit var currentTemp: TextView
    private lateinit var currentSky: TextView
    private lateinit var currentAQI: TextView
    private lateinit var forecastLayout: LinearLayout
    private lateinit var nowLayout: RelativeLayout
    private lateinit var coldRiskText: TextView
    private lateinit var carWashingText: TextView
    private lateinit var dressingText: TextView
    private lateinit var ultravioletText: TextView
    private lateinit var weatherLayout: ScrollView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var navBtn: Button
    private lateinit var drawerLayout: DrawerLayout



    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 将activity布局显示在状态栏上面
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        // 将状态栏显示为透明
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)

        placeName = findViewById(R.id.placeName)
        currentTemp = findViewById(R.id.currentTemp)
        currentSky = findViewById(R.id.currentSky)
        currentAQI = findViewById(R.id.currentAQI)
        forecastLayout = findViewById(R.id.forecastLayout)
        nowLayout = findViewById(R.id.nowLayout)
        coldRiskText = findViewById(R.id.coldRiskText)
        carWashingText = findViewById(R.id.carWashingText)
        dressingText = findViewById(R.id.dressingText)
        ultravioletText = findViewById(R.id.ultravioletText)
        weatherLayout = findViewById(R.id.weatherLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        navBtn = findViewById(R.id.navBtn)
        drawerLayout = findViewById(R.id.drawerLayout)


        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing = false
        })
        swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                // 当滑动菜单被隐藏时，隐藏输入法
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,
                             InputMethodManager.HIDE_NOT_ALWAYS)
            }

        })
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime  // Realtime对象
        val daily = weather.daily    // Daily对象

        // 填充now.xml中的布局
        val currentTempText = "${realtime.temperature.toInt()} °C"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数${realtime.airQuality.aqi.chn}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]  // "skycon": [{"value": "CLOUDY", "date": "2019-10-20T00.00+08.00"}, ·····]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                                      forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} °C"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }

        /*
        “life_index": {
        "coldRisk": [{"desc": "易发"}, ·····],
        "carWashing": [{"desc": "适宜"}, ·····],
        "ultraviolet": [{"desc": "无"}, ·····],
        "dressing": [{"desc": "舒适"}, ·····]
         */
        // 填充life_index.xml中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc

        weatherLayout.visibility = View.VISIBLE
    }
}