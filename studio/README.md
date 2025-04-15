# CaptionCraft

CaptionCraft is a social media Android application where users can post images and videos, and other users can suggest captions for them. The most liked caption becomes the official caption for the post.

## Features

- User authentication
- Post images and videos
- Suggest captions for posts
- Like/dislike captions
- User profiles
- Search for users
- Follow/unfollow users

## Tech Stack

- Kotlin
- Jetpack Compose for UI
- Firebase (Authentication, Firestore, Storage)
- Hilt for dependency injection
- Coil for image loading
- MVVM architecture
- Material Design 3

## Setup

1. Clone the repository
2. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
3. Add an Android app to your Firebase project with package name `com.example.captioncraft`
4. Download the `google-services.json` file and place it in the `app` directory
5. Replace the placeholder Firebase configuration in `app/google-services.json` with your actual configuration
6. Build and run the project

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/captioncraft/
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── feed/
│   │   │   │   │   ├── profile/
│   │   │   │   │   ├── search/
│   │   │   │   │   └── upload/
│   │   │   │   └── theme/
│   │   │   ├── MainActivity.kt
│   │   │   └── CaptionCraftApp.kt
│   │   └── res/
│   └── androidTest/
└── build.gradle.kts
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 