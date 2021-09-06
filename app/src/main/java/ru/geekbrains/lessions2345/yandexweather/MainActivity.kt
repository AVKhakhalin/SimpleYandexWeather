package ru.geekbrains.lessions2345.yandexweather

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    private val retrofitImpl: RetrofitImpl = RetrofitImpl()
    private var temperatureField: TextView? = null
    private var feelsTemperatureField: TextView? = null
    private var conditionWeatherField: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temperatureField = findViewById(R.id.current_temperature_field)
        feelsTemperatureField = findViewById(R.id.feel_temperature_field)
        conditionWeatherField = findViewById(R.id.condition_text_view)
        // Отправка запроса на получение данных
        sendServerRequest()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    private fun sendServerRequest() {
        retrofitImpl.getWeatherApi()
            .getWeather(
                // НУЖНО ЗАМЕНИТЬ ЗДЕСЬ НА ВАШ "X-Yandex-API-Key" КЛЮЧ
                "8df85a2d-de57-4e99-be0f-4d7cb50a67ef",
                52.52000659999999,
                13.4049539999999975,
                "ru_RU"
            )
            .enqueue(object :
                Callback<DataModel> {

                override fun onResponse(
                    call: Call<DataModel>,
                    response: Response<DataModel>
                ) {
                    if ((response.isSuccessful) && (response.body() != null)) {
                        if ((temperatureField != null) && (feelsTemperatureField != null) && (conditionWeatherField != null)) {
                            renderData(response.body(), null)
                        }
                    } else {
                        renderData(null, Throwable("Ответ от сервера пустой"))
                    }
                }

                override fun onFailure(call: Call<DataModel>, t: Throwable) {
                    renderData(null, t)
                }
            })
    }

    private fun renderData(dataModel: DataModel?, error: Throwable?) {
        if ((dataModel == null) || (dataModel.fact == null) || (error != null)) {
            Toast.makeText(this, error?.message, Toast.LENGTH_LONG).show() // Ошибка
        } else {
            val  fact: Fact? = dataModel.fact
            val temperature: Int? = fact?.temp
            if (temperature == null) {
                // "Поле пустое")
            } else {
                temperatureField?.text = temperature.toString()
            }

            val feelsLike: Int? = fact?.feels_like
            if (feelsLike == null) {
                // "Поле пустое"
            } else {
                    feelsTemperatureField?.text = feelsLike.toString()
            }

            val condition: String? = fact?.condition
            if (condition.isNullOrEmpty()) {
                //"Описание пустое"
            } else {
                when (condition) {
                    "clear" -> conditionWeatherField?.text = "Ясно"
                    "partly-cloudy" -> conditionWeatherField?.text = "Редкие облака"
                    "cloudy" -> conditionWeatherField?.text = "Облачно"
                    "overcast" -> conditionWeatherField?.text = "Пасмурно"
                    "partly-cloudy-and-light-rain" -> conditionWeatherField?.text = "Редкие облака и лёгкий дождь"
                    "cloudy-and-light-rain" -> conditionWeatherField?.text = "Облачно и лёгкий дождь"
                    "overcast-and-light-rain" -> conditionWeatherField?.text = "Пасмурно и лёгкий дождь"
                    "partly-cloudy-and-rain" -> conditionWeatherField?.text = "Редкие облака и дождь"
                    "overcast-and-rain" -> conditionWeatherField?.text = "Пасмурно и дождь"
                    "cloudy-and-rain" -> conditionWeatherField?.text = "Облачно и дождь"
                    "overcast-thunderstorms-with-rain" -> conditionWeatherField?.text = "Пасмурно, гром и дождь"
                    "overcast-and-wet-snow" -> conditionWeatherField?.text = "Пасмурно и мокрый снег"
                    "partly-cloudy-and-light-snow" -> conditionWeatherField?.text = "Редкие облака и лёгкий снег"
                    "partly-cloudy-and-snow" -> conditionWeatherField?.text = "Редкие облака и снег"
                    "overcast-and-snow" -> conditionWeatherField?.text = "Пасмурно и снег"
                    "cloudy-and-light-snow" -> conditionWeatherField?.text = "Облачно и лёгкий снег"
                    "overcast-and-light-snow" -> conditionWeatherField?.text = "Пасмурно и лёгкий снег"
                    "cloudy-and-snow" -> conditionWeatherField?.text = "Облачно и снег"
                    else -> conditionWeatherField?.text = "Ясно"
                }
            }
        }
    }
}

data class DataModel(
    val fact: Fact?
)

data class Fact(
    val temp: Int?,
    val feels_like: Int?,
    val condition: String?
)

interface WeatherAPI {
    @GET("v2/informers")
    fun getWeather(
        @Header("X-Yandex-API-Key") token: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String
    ): Call<DataModel>
}

class RetrofitImpl {
    fun getWeatherApi(): WeatherAPI {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.weather.yandex.ru/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()
        return retrofit.create(WeatherAPI::class.java)
    }
}