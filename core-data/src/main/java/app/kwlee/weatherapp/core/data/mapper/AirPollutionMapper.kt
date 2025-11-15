package app.kwlee.weatherapp.core.data.mapper

import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionComponentsDto
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionDataDto
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionResponseDto
import app.kwlee.weatherapp.core.data.remote.dto.AirQualityMainDto
import app.kwlee.weatherapp.core.domain.model.AirPollution


fun AirPollutionResponseDto.toDomain(): AirPollution? {
    val airPollutionData = list?.firstOrNull() ?: return null
    val components = airPollutionData.components ?: return null
    val main = airPollutionData.main

    return AirPollution(
        airQualityIndex = main?.airQualityIndex,
        co = components.co,
        no = components.no,
        no2 = components.no2,
        o3 = components.o3,
        so2 = components.so2,
        pm25 = components.pm25 ?: 0.0,
        pm10 = components.pm10,
        nh3 = components.nh3,
    )
}

