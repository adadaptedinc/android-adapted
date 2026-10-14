package com.adadapted.androidadapted.adadapted

/** How the SDK is started. Shown in Zone Lab so testers can see what they're running against. */
data class AdAdaptedConfig(
    val appId: String,
    val isProduction: Boolean,
    val customIdentifier: String,
    val isKeywordInterceptEnabled: Boolean,
    val arePayloadsEnabled: Boolean,
    val isDebugLoggingEnabled: Boolean,
) {
    val environmentName: String
        get() = if (isProduction) "Production" else "Development"

    companion object {
        /** The sandbox app key, which serves the test ads and keywords. */
        val Default = AdAdaptedConfig(
            appId = "7D58810X6333241C",
            isProduction = false,
            customIdentifier = "AndroidAdapted",
            isKeywordInterceptEnabled = true,
            arePayloadsEnabled = true,
            isDebugLoggingEnabled = true,
        )
    }
}
