
# 📰 News App - Android Firebase News Platform

Welcome to **News App**, a modern, real-time news platform built with **Java**, **Firebase**, and **Material Design** for Android. This app brings users a sleek and dynamic news experience with personalized categories, real-time content updates, push notifications, and smooth UI animations.

---

## 📱 Features

✅ **Firebase Authentication**  
Secure login & registration using Firebase Email/Password.

✅ **Realtime News Feed**  
Live updates using Firebase Realtime Database with categorized news and ViewPager2 for featured articles.

✅ **Push Notifications**  
Smart notifications powered by Firebase Cloud Messaging (FCM), with a user-controlled mute/unmute toggle.

✅ **Modern UI**  
Clean UI with `ConstraintLayout`, `CardView`, and responsive layouts inspired by Material Design.

✅ **User Preferences**  
"Remember Me" login, saved mute state using `SharedPreferences`.

✅ **Modular Code**  
Separation of concerns across activities like `LoginActivity`, `SignupActivity`, `HomeActivity`, and `NewsActivity`.

---

## 📸 Screenshots

| Splash Screen | Login | Home | News Detail |
|---------------|-------|------|-------------|
| ![splash](assets/screens/splash.png) | ![login](assets/screens/login.png) | ![home](assets/screens/home.png) | ![news](assets/screens/news_detail.png) |

---

## 🔧 Tech Stack

- **Language:** Java  
- **IDE:** Android Studio  
- **Database:** Firebase Realtime Database  
- **Auth:** Firebase Authentication  
- **Notifications:** Firebase Cloud Messaging (FCM)  
- **Design:** ConstraintLayout, Material Icons, ViewPager2  
- **Others:** SharedPreferences, Notification Channels

---

## 🚀 Setup & Installation

1. **Clone the repo**
   ```bash
   git clone https://github.com/yourusername/news-app.git
   cd news-app
   ```

2. **Open in Android Studio**

3. **Connect Firebase**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a project and register your app
   - Download `google-services.json` and place it inside `app/` directory

4. **Build & Run**
   - Sync Gradle
   - Run on a device or emulator

---

## 🔔 Firebase Notifications

- Users receive **push notifications** from Firebase.
- Notifications are only shown when **mute is OFF**.
- Controlled via a toggle in the app and stored in `SharedPreferences`.

---

## 🌐 Project Structure

```
├── MainActivity.java
├── LoginActivity.java
├── SignupActivity.java
├── HomeActivity.java
├── NewsActivity.java
├── MyFirebaseMessagingService.java
├── utils/
│   └── SharedPrefsHelper.java
├── model/
│   └── NewsModel.java
├── res/
│   ├── animator/
│   ├── layout/
│   ├── drawable/
│   ├── font/
│   └── values/
└── AndroidManifest.xml
```

---

## 🛡️ Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

---

## 🙌 Author

**Your Name**  
Trainee Full Stack Developer  
University of Colombo / University of Moratuwa  
[LinkedIn](https://linkedin.com/in/your-profile) • [GitHub](https://github.com/yourusername)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
