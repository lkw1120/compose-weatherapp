package app.kwlee.weatherapp.core.data.mapper

import app.kwlee.weatherapp.core.data.local.entity.FavoriteLocationEntity
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation


fun FavoriteLocationEntity.toDomain(): FavoriteLocation = FavoriteLocation(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    isPrimary = isPrimary,
)


fun FavoriteLocation.toEntity(): FavoriteLocationEntity = FavoriteLocationEntity(
    id = id ?: 0,
    name = name,
    latitude = latitude,
    longitude = longitude,
    isPrimary = isPrimary,
)

