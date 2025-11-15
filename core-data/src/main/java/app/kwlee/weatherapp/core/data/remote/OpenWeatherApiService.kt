package app.kwlee.weatherapp.core.data.remote

import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionResponseDto
import app.kwlee.weatherapp.core.data.remote.dto.GeocodingLocationDto
import app.kwlee.weatherapp.core.data.remote.dto.OneCallResponseDto
import app.kwlee.weatherapp.core.data.remote.dto.ReverseGeocodingDto
import retrofit2.http.GET
import retrofit2.http.Query


interface OpenWeatherApiService {

    @GET("data/3.0/onecall")
    suspend fun fetchOneCallWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "minutely",
        @Query("lang") language: String,
    ): OneCallResponseDto


    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String,
        @Query("lang") language: String,
    ): List<ReverseGeocodingDto>


    @GET("geo/1.0/direct")
    suspend fun geocode(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String,
        @Query("lang") language: String,
    ): List<GeocodingLocationDto>


    @GET("data/2.5/air_pollution")
    suspend fun fetchAirPollution(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
    ): AirPollutionResponseDto
}

