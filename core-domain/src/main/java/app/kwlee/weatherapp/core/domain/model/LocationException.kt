package app.kwlee.weatherapp.core.domain.model

class LocationPermissionMissingException : Exception("Location permission is not granted.")

class LocationProviderDisabledException : Exception("Location providers are disabled.")

