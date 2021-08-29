package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName
import java.util.*

/*
获取未来几天天气信息接口所返回的JSON格式
{
     "status": "ok",
     "result": {
         "daily": {
              "temperature": [{"max": 25.7, "min": 20.3}, ·····],
              "skycon": [{"value": "CLOUDY", "date": "2019-10-20T00.00+08.00"}, ·····]，
              “life_index": {
                   "coldRisk": [{"desc": "易发"}, ·····],
                   "carWashing": [{"desc": "适宜"}, ·····],
                   "ultraviolet": [{"desc": "无"}, ·····],
                   "dressing": [{"desc": "舒适"}, ·····]
              }
         }   // daily
     }   // result
}
 */

data class DailyResponse(val status: String, val result: Result) {

    data class Result(val daily: Daily)

    data class Daily(val temperature: List<Temperature>, val skycon: List<skycon>,
                     @SerializedName("life_index") val lifeIndex: LifeIndex)

    data class Temperature(val max: Float, val min: Float)

    data class skycon(val value: String, val date: Date)

    data class LifeIndex(val coldRisk: List<LifeDescription>, val carWashing: List<LifeDescription>,
                         val ultraviolet: List<LifeDescription>, val dressing: List<LifeDescription>)

    data class LifeDescription(val desc: String)
}