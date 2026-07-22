# Release Signing Guide

This project is configured to generate an unsigned release APK by default.

## Signing
1. Generate a keystore using `keytool` or via Android Studio's "Generate Signed Bundle/APK" wizard.
2. Store the generated `.jks` file securely, outside the version control system.
3. Modify the app's `build.gradle.kts` to specify the signing config using local properties or environment variables.

Example:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file(project.property("RELEASE_STORE_FILE") as String)
        storePassword = project.property("RELEASE_STORE_PASSWORD") as String
        keyAlias = project.property("RELEASE_KEY_ALIAS") as String
        keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
    }
}
```
