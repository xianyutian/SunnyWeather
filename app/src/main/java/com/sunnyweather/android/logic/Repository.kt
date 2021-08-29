package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import java.lang.RuntimeException

// 仓库层的主要作用是判断调用方请求的数据应该是从本地数据源中获取还是从网络获取 在本地没有缓存数据的情况下就去网络层获取
// 不过搜索城市数据的请求没有太多缓存的必要，每次都发起网络请求获取最新的数据即可

object Repository {

    // liveData函数自动构建并返回一个LiveData对象
    // Dispatchers.IO为线程参数，使代码块中的代码在子线程中运行
    // (Android不允许在主线程中进行网络请求，读写数据库之类的本地数据操作也应该在子线程中执行)
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places   // 得到了搜寻出的place
                Result.success(places)   // 包装获取的城市数据列表
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)   // 类似于LiveData的setValue来通知数据变化
    }

}