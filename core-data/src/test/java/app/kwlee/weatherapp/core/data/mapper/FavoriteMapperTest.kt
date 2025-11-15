package app.kwlee.weatherapp.core.data.mapper

import app.kwlee.weatherapp.core.data.local.entity.FavoriteLocationEntity
import app.kwlee.weatherapp.core.data.mapper.toDomain
import app.kwlee.weatherapp.core.data.mapper.toEntity
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import org.junit.Assert.assertEquals
import org.junit.Test


class FavoriteMapperTest {

    @Test
    fun `toDomain converts entity to domain model correctly`() {
        val entity = FavoriteLocationEntity(
            id = 1L,
            name = "Seoul",
            latitude = 37.5665,
            longitude = 126.9780,
            isPrimary = true,
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("Seoul", domain.name)
        assertEquals(37.5665, domain.latitude, 0.0001)
        assertEquals(126.9780, domain.longitude, 0.0001)
        assertEquals(true, domain.isPrimary)
    }

    @Test
    fun `toEntity converts domain to entity correctly`() {
        val domain = FavoriteLocation(
            id = 1L,
            name = "Seoul",
            latitude = 37.5665,
            longitude = 126.9780,
            isPrimary = true,
        )

        val entity = domain.toEntity()

        assertEquals(1L, entity.id)
        assertEquals("Seoul", entity.name)
        assertEquals(37.5665, entity.latitude, 0.0001)
        assertEquals(126.9780, entity.longitude, 0.0001)
        assertEquals(true, entity.isPrimary)
    }

    @Test
    fun `toEntity uses zero for id when domain id is null`() {
        val domain = FavoriteLocation(
            id = null,
            name = "Seoul",
            latitude = 37.5665,
            longitude = 126.9780,
        )

        val entity = domain.toEntity()

        assertEquals(0L, entity.id)
        assertEquals("Seoul", entity.name)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val originalDomain = FavoriteLocation(
            id = 1L,
            name = "Busan",
            latitude = 35.1796,
            longitude = 129.0756,
            isPrimary = false,
        )

        val entity = originalDomain.toEntity()
        val convertedDomain = entity.toDomain()

        assertEquals(originalDomain, convertedDomain)
    }
}

