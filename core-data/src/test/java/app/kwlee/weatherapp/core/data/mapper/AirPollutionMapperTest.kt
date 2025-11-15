package app.kwlee.weatherapp.core.data.mapper

import app.kwlee.weatherapp.core.data.mapper.toDomain
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionComponentsDto
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionDataDto
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionResponseDto
import app.kwlee.weatherapp.core.data.remote.dto.AirQualityMainDto
import app.kwlee.weatherapp.core.domain.model.AirPollution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test


class AirPollutionMapperTest {

    @Test
    fun `toDomain converts response to domain model correctly`() {
        val response = createTestResponse(
            airQualityIndex = 2,
            pm25 = 15.5,
            pm10 = 25.0,
            co = 200.0,
        )

        val domain = response.toDomain()

        assertNotNull(domain)
        assertEquals(2, domain!!.airQualityIndex ?: 0)
        assertEquals(15.5, domain.pm25 ?: 0.0, 0.01)
        assertEquals(25.0, domain.pm10 ?: 0.0, 0.01)
        assertEquals(200.0, domain.co ?: 0.0, 0.01)
    }

    @Test
    fun `toDomain returns null when list is empty`() {
        val response = AirPollutionResponseDto(
            coordinates = null,
            list = emptyList(),
        )

        val domain = response.toDomain()

        assertNull(domain)
    }

    @Test
    fun `toDomain returns null when list is null`() {
        val response = AirPollutionResponseDto(
            coordinates = null,
            list = null,
        )

        val domain = response.toDomain()

        assertNull(domain)
    }

    @Test
    fun `toDomain returns null when components is null`() {
        val response = AirPollutionResponseDto(
            coordinates = null,
            list = listOf(
                AirPollutionDataDto(
                    timestamp = null,
                    main = null,
                    components = null,
                ),
            ),
        )

        val domain = response.toDomain()

        assertNull(domain)
    }

    @Test
    fun `toDomain uses default value 0_0 for pm25 when null`() {
        val response = createTestResponse(
            pm25 = null,
        )

        val domain = response.toDomain()

        assertNotNull(domain)
        assertEquals(0.0, domain!!.pm25 ?: 0.0, 0.01)
    }

    @Test
    fun `toDomain handles all component values`() {
        val response = createTestResponse(
            airQualityIndex = 3,
            co = 100.0,
            no = 10.0,
            no2 = 20.0,
            o3 = 30.0,
            so2 = 40.0,
            pm25 = 50.0,
            pm10 = 60.0,
            nh3 = 70.0,
        )

        val domain = response.toDomain()

        assertNotNull(domain)
        assertEquals(3, domain!!.airQualityIndex ?: 0)
        assertEquals(100.0, domain.co ?: 0.0, 0.01)
        assertEquals(10.0, domain.no ?: 0.0, 0.01)
        assertEquals(20.0, domain.no2 ?: 0.0, 0.01)
        assertEquals(30.0, domain.o3 ?: 0.0, 0.01)
        assertEquals(40.0, domain.so2 ?: 0.0, 0.01)
        assertEquals(50.0, domain.pm25 ?: 0.0, 0.01)
        assertEquals(60.0, domain.pm10 ?: 0.0, 0.01)
        assertEquals(70.0, domain.nh3 ?: 0.0, 0.01)
    }

    private fun createTestResponse(
        airQualityIndex: Int? = null,
        co: Double? = null,
        no: Double? = null,
        no2: Double? = null,
        o3: Double? = null,
        so2: Double? = null,
        pm25: Double? = null,
        pm10: Double? = null,
        nh3: Double? = null,
    ): AirPollutionResponseDto {
        return AirPollutionResponseDto(
            coordinates = null,
            list = listOf(
                AirPollutionDataDto(
                    timestamp = null,
                    main = airQualityIndex?.let { AirQualityMainDto(airQualityIndex = it) },
                    components = AirPollutionComponentsDto(
                        co = co,
                        no = no,
                        no2 = no2,
                        o3 = o3,
                        so2 = so2,
                        pm25 = pm25,
                        pm10 = pm10,
                        nh3 = nh3,
                    ),
                ),
            ),
        )
    }
}

