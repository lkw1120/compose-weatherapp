# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================
# Project-specific classes
# ============================================
# Keep Application class
-keep class app.kwlee.weatherapp.WeatherApp { *; }

# Keep BuildConfig
-keep class app.kwlee.weatherapp.BuildConfig { *; }

# ============================================
# Retrofit service interface
# ============================================
-keep class app.kwlee.weatherapp.core.data.remote.OpenWeatherApiService { *; }

# ============================================
# Moshi DTO classes
# ============================================
-keep class app.kwlee.weatherapp.core.data.remote.dto.** { *; }
-keepclassmembers class app.kwlee.weatherapp.core.data.remote.dto.** {
    <fields>;
}

# Keep custom Moshi adapters
-keep class app.kwlee.weatherapp.core.data.remote.moshi.** { *; }

# ============================================
# Room Database
# ============================================
-keep class app.kwlee.weatherapp.core.data.local.WeatherDatabase { *; }

# Keep Room entities
-keep class app.kwlee.weatherapp.core.data.local.entity.** { *; }
-keepclassmembers class app.kwlee.weatherapp.core.data.local.entity.** {
    <fields>;
}

# Keep Room DAOs
-keep interface app.kwlee.weatherapp.core.data.local.dao.** { *; }
-keepclassmembers interface app.kwlee.weatherapp.core.data.local.dao.** {
    <methods>;
}

# ============================================
# Dagger Hilt modules
# ============================================
-keep @dagger.Module class app.kwlee.weatherapp.core.di.** { *; }
-keep @dagger.Module class app.kwlee.weatherapp.core.data.di.** { *; }
-keep @dagger.Module class app.kwlee.weatherapp.di.** { *; }

# ============================================
# Glance Widget
# ============================================
-keep class app.kwlee.weatherapp.feature.widget.glance.** { *; }
-keep class app.kwlee.weatherapp.feature.widget.glance.WeatherGlanceAppWidgetReceiver { *; }

# ============================================
# Domain Models
# ============================================
-keep class app.kwlee.weatherapp.core.domain.model.** { *; }
-keepclassmembers class app.kwlee.weatherapp.core.domain.model.** {
    <fields>;
}

# ============================================
# Common
# ============================================
-keep class app.kwlee.weatherapp.core.common.** { *; }

# ============================================
# Remove logging in release builds
# ============================================
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
