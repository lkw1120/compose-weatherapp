# Weather App

A modern Android weather application built with Jetpack Compose, featuring real-time weather data, location-based forecasts, and a home screen widget.

## Overview

This weather app provides comprehensive weather information including current conditions, hourly and daily forecasts, weather alerts, and air quality data. It uses the OpenWeatherMap API to fetch weather data and supports multiple units and languages.

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK (API level 26+)
- OpenWeatherMap API key ([Get one here](https://openweathermap.org/api))

### Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd compose-weatherapp
   ```

2. Create `local.properties` file in the root directory:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   OPEN_WEATHER_API_KEY=your_api_key_here
   ```

3. Open the project in Android Studio and sync Gradle files.

4. Run the app on an emulator or physical device.

**Note**: The `local.properties` file is already in `.gitignore` and will not be committed to the repository.

## Architecture

The app follows **Clean Architecture** principles with a modular structure:

- **Core Modules**
  - `core`: Common utilities, network monitoring, location services
  - `core-domain`: Domain models and use cases (business logic)
  - `core-data`: Data layer with repositories, API clients, and local database
  - `core-ui`: Shared UI components and mappers

- **Feature Modules**
  - `feature-main`: Main weather screen with current conditions and forecasts
  - `feature-search`: Location search functionality
  - `feature-bookmarks`: Favorite locations management
  - `feature-settings`: App settings and preferences
  - `feature-weather-preview`: Weather preview bottom sheet
  - `feature-widget`: Home screen widget

- **Navigation Module**
  - `navigation`: App-wide navigation setup

The architecture follows **MVVM** pattern with:
- **ViewModels**: Platform-independent business logic
- **UI Layer**: Jetpack Compose screens and components
- **Repository Pattern**: Data abstraction layer
- **Use Cases**: Single responsibility business operations

## Tech Stack

### UI & Framework
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Material Design components
- **Navigation Compose** - Type-safe navigation
- **Coil** - Image loading library

### Dependency Injection
- **Hilt** - Dependency injection framework

### Networking
- **Retrofit** - HTTP client
- **Moshi** - JSON parsing
- **OkHttp** - HTTP client with logging interceptor

### Local Storage
- **Room** - Local database for caching weather data
- **DataStore** - Key-value storage for settings

### Other Libraries
- **Work Manager** - Background tasks for widget updates
- **Glance** - App widget framework
- **Timber** - Logging utility
- **TedPermission** - Permission handling

## Features

- **Current Weather**: Real-time weather conditions for current location
- **Location Search**: Search and add multiple locations
- **Favorites**: Save and manage favorite locations
- **Forecasts**: Hourly (24 hours) and daily (8 days) weather forecasts
- **Weather Alerts**: Severe weather warnings and advisories
- **Air Quality**: Air pollution index (PM2.5)
- **Weather Widget**: Home screen widget with current weather
- **Settings**: Customizable units (temperature, wind speed, precipitation, etc.) and time format
- **Multi-language**: Supports multiple languages via OpenWeatherMap API

## Contributing

Contributions are welcome! Feel free to submit issues and pull requests.

## License

```
Copyright 2025 lkw1120

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
