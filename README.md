# CallRecording 📞🎙️

## Overview
CallRecording is an Android application written in Kotlin that enables users to record phone calls. The application utilizes the Android TelephonyManager API to intercept and record incoming and outgoing calls. Recorded calls are uploaded using Retrofit, leveraging WorkManager for background task management.

## Compatibility
Please note that CallRecording has been tested and verified to work with Android version 8 (Oreo). Due to system-level restrictions introduced in later versions of Android, the application may not function as expected or may be incompatible with newer Android versions.

## Features 🚀
- Record incoming and outgoing phone calls.
- Seamless integration with Android system services.
- Upload recorded calls using Retrofit.
- Background task management with WorkManager.

## Usage 🛠️
To use CallRecording in your Android project, follow these steps:
1. Clone the repository to your local machine.
2. Open the project in Android Studio.
3. Ensure your device or emulator is running Android version 8 (Oreo).
4. Build and run the application on your device or emulator.
5. Grant necessary permissions for call recording when prompted.
6. Make or receive phone calls to test the recording functionality.

## Libraries Used 📚
- **TelephonyManager**: Android API for managing telephone calls, allowing access to telephony services on the device.
- **Retrofit**: A type-safe HTTP client for Android and Java that simplifies communication with HTTP APIs by turning your HTTP API into a Java interface.
- **WorkManager**: An Android library for managing deferrable, asynchronous tasks and specifying constraints on when those tasks should run.

## Contributing 🤝
Contributions to CallRecording are welcome! If you'd like to contribute new features, enhancements, or bug fixes, please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or fix.
3. Make your changes and commit them with descriptive messages.
4. Push your changes to your fork.
5. Submit a pull request to the main repository's `main` branch.

## License 📝
This project is licensed under the [Apache License 2.0](LICENSE).
