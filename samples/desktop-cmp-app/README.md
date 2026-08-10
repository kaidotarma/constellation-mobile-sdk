## Constellation SDK - Desktop Sample Application

Project contains Desktop sample application that demonstrates the usage of the SDK.

Build the desktop sample with Gradle `9.4.0` and JDK 17 or newer using the repository's Gradle Wrapper.

To run the sample application, you need to provide necessary configuration: [SDKConfig.kt](../../samples/base-cmp-app/src/commonMain/kotlin/com/pega/constellation/sdk/kmp/samples/basecmpapp/SDKConfig.kt)

```kotlin
object SDKConfig {
    ...
    const val PEGA_URL = "https://insert-url-here.example/prweb"
    ...
}
```

For more information about the configuration, please refer to the [Configuring sample mobile application](../../docs/configure-sample-mobile-apps.md) document.
