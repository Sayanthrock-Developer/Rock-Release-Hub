# Rock Release Hub Setup Guide

## Requirements
- Android Studio Iguana | 2023.2.1 or newer
- JDK 17
- Minimum SDK 29

## Architecture Overview
This app utilizes a clean architecture MVVM pattern heavily relying on Jetpack Compose, Coroutines/Flow, Room Database, and Hilt Dependency Injection.
It operates under a multi-module setup consisting of core functionalities (`core-*`), distinct features (`feature-*`), and the orchestrating `app` module.

## Building the Project
1. Clone this repository.
2. Open the project in Android Studio.
3. Sync project with Gradle files.
4. Run standard `./gradlew assembleDebug` to build the testing APK.
