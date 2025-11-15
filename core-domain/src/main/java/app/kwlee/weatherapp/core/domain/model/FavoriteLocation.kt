package app.kwlee.weatherapp.core.domain.model


data class FavoriteLocation(
    val id: Long? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isPrimary: Boolean = false,
)

