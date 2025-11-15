package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.LocationResult
import app.kwlee.weatherapp.core.domain.model.LocationPermissionMissingException
import app.kwlee.weatherapp.core.domain.model.LocationProviderDisabledException
import app.kwlee.weatherapp.core.domain.repository.LocationRepository
import javax.inject.Inject


/**
 * UseCase for obtaining the current device location.
 *
 * Encapsulates business logic for location retrieval and error handling.
 * Transforms repository exceptions into domain-specific LocationResult types:
 * - LocationPermissionMissingException -> PermissionRequired
 * - LocationProviderDisabledException -> ProviderDisabled
 * - Other exceptions -> Error
 *
 * This abstraction allows the UI layer to handle location errors consistently
 * without knowing about specific exception types.
 *
 * @param locationRepository Repository for location operations
 */
class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): LocationResult {
        val result = locationRepository.getCurrentLocation()
        return result.fold(
            onSuccess = { location -> LocationResult.Success(location) },
            onFailure = { throwable ->
                when (throwable) {
                    is LocationPermissionMissingException -> LocationResult.PermissionRequired
                    is LocationProviderDisabledException -> LocationResult.ProviderDisabled
                    else -> LocationResult.Error(throwable)
                }
            },
        )
    }
}

