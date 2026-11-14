<div align="center">

<img src="app/src/main/res/drawable/ic_launcher_art.png" alt="Mihrab App Icon" width="128" height="128" />

# Mihrab (محراب)
### Islamic Prayer Times, Qibla Compass, Holy Quran (Read & Listen) & Daily Dhikr for Android

[![Platform](https://img.shields.io/badge/Platform-Android%20(API%2024%2B)-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?logo=kotlin&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203%20Expressive-4285F4?logo=jetpackcompose&logoColor=white)](#)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%7C%20Offline--First-00897B)](#)

**Mihrab (محراب)** is a complete, offline-first Islamic worship companion for Android built with **Kotlin** and **Jetpack Compose** following **Google Material 3 Expressive Design**. It combines high-precision astronomical prayer calculations, a hardware-accelerated Qibla compass, a 604-page Medina Mushaf reader, an expressive Quran audio player with **57+ authentic reciters (including Islam Sobhi — إسلام صبحي)**, comprehensive daily Azkar & Tasbeeh, lock-screen prayer notifications, and **5 resizable Home Screen widgets**.

</div>

---

## ✨ Key Features

### 🕌 1. Prayer Times Dashboard & Astronomical Engine
- **100% Offline Solar Position Calculations**: Computes exact daily prayer times (**Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha**, plus **Imsak & Midnight**) directly from the device's GPS coordinates or a built-in global city catalog—no internet connection required.
- **Calculation Methods Supported**:
  - Muslim World League (MWL)
  - Egyptian General Authority of Survey
  - Umm Al-Qura University, Makkah
  - University of Islamic Sciences, Karachi
  - Islamic Society of North America (ISNA)
  - Dubai, Qatar & Kuwait
- **Juristic & High-Latitude Support**:
  - Standard (**Shafi'i, Maliki, Hanbali**) and **Hanafi** Asr shadow-ratio methods.
  - High-latitude rules (**Middle of the Night**, **One-Seventh of the Night**, **Angle-Based**) and manual per-prayer minute adjustments (`-30m` to `+30m`).
- **Live Next-Prayer Hero Card**: Real-time `HH:MM:SS` countdown, progress bar, Hijri & Gregorian dates, local weather chip, and daily 5-prayer worship completion tracker with streak badges.

---

### 🧭 2. Real-Time Qibla Compass
- **Great-Circle Bearing & Distance**: Calculates exact Qibla bearing (`°` from True North) and geodesic distance (`km`) from the user's location to the Holy Kaaba (`21.4225° N, 39.8262° E`).
- **Sensor Fusion**: Uses Android's **Rotation Vector** sensor with automatic fallback to **Accelerometer + Magnetometer**, smooth shortest-arc angular interpolation across the `0°/360°` boundary, and live sensor accuracy diagnostics.
- **Alignment Confirmation**: Visual glow and subtle haptic feedback when the device is aligned with the Qibla direction, plus an offline Qibla vector map card.

---

### 📖 3. Holy Quran — Read & Listen (`قراءة واستماع`)
Switch seamlessly between **Read Mushaf** and **Listen (57+ Reciters)** via the top pill selector:

#### **Read Mushaf (`قراءة المصحف`)**
- **604-Page Medina Mushaf Layout (`مصحف المدينة المنورة`)**: Authentic page-by-page Mushaf view with accurate Surah headers, ornamental Bismillah banners (automatically stripped from the first verse of Surahs 2–114 except Al-Fatihah and At-Tawbah), Juz indicators, and Arabic-Indic verse markers (`۝١`).
- **Tanzil Script Options**: Toggle between **Uthmani (`عثماني`)** and **Simple (`إملائي`)** scripts, adjustable Arabic font size, English translation toggle, night reading mode, full-text search, bookmarks, and full 114-Surah offline caching.

#### **Listen (`استماع — ٥٧+ قارئ`)**
- **Material 3 Expressive Audio Player**:
  - Browse **Surahs (114)**, **Reciters (57+)**, **Favorites**, and **Downloaded Offline** tracks.
  - Active track rows morph into full pill shapes (`CircleShape`) with dynamic primary-color highlighting.
  - Floating **Expressive Mini-Player Pill** and expandable **Full-Screen Now Playing** sheet featuring a large circular emblem, **Animated Wavy Progress Scrubber** with vertical pill thumb, squircle Play/Pause button, tall rounded-pill `-30s`/`+30s` and skip controls, and volume/mute pill controls.
- **57+ Authentic Quran Reciters**:
  - Includes **Islam Sobhi (`إسلام صبحي`)**, Mishary Rashid Alafasy, Abdul Basit Abdul Samad (Murattal & Mujawwad), Mahmoud Khalil Al-Husary (Murattal & Mujawwad), Mohamed Siddiq Al-Minshawi (Murattal & Mujawwad), Maher Al-Muaiqly, Abdul Rahman Al-Sudais, Saud Al-Shuraim, Yasser Al-Dosari, Nasser Al-Qatami, Saad Al-Ghamdi, Ahmed Al-Ajmi, Fares Abbad, Abu Bakr Al-Shatri, Hazza Al-Balushi, Raad Al-Kurdi, Idris Abkar, Khalid Al-Jalil, Mansour Al-Salimi, Noreen Mohammad Siddiq, and many more.
- **Screen-Off Background Playback & Offline Downloads**:
  - Powered by a wake-locked media engine and foreground `mediaPlayback` service with lock-screen transport controls (`Previous`, `Play/Pause`, `Next`, `Stop`).
  - Download any Surah MP3 per reciter for 100% offline listening.

---

### 📿 4. Dhikr, Azkar, Duas & Digital Tasbeeh
- **Interactive Digital Tasbeeh Counter**: Scalloped expressive counter button with target presets (`33`, `99`, `100`, `1000`), haptic feedback on every tap and cycle completion, total lifetime counter, and custom Dhikr creation.
- **Complete Authentic Azkar & Duas**:
  - **Morning Azkar (`أذكار الصباح`)**
  - **Evening Azkar (`أذكار المساء`)**
  - **After-Prayer Azkar (`أذكار بعد الصلاة`)**
  - **Sleep & Waking Azkar (`أذكار النوم والاستيقاظ`)**
  - **Comprehensive Quranic & Prophetic Duas (`أدعية قرآنية ونبوية جامعة`)** with per-item repetition counters and progress tracking.

---

### 🔔 5. Native Prayer Notifications & Lock-Screen Alerts
- **Native System Notification & Alarm Sounds**: Uses the device's native notification, alarm, or ringtone sounds via `RingtoneManager` alongside Android notification channels.
- **Lock-Screen Visibility (`VISIBILITY_PUBLIC`)**: Displays both active prayer alerts and a persistent lock-screen next-prayer status notification with upcoming prayer times.
- **Reliable Scheduling**: Exact alarm scheduling via `AlarmManager` with automatic rescheduling across device reboots and time/timezone changes.

---

### 📱 6. Five Resizable Material 3 Expressive Home Screen Widgets
Built with **Jetpack Glance**, all 5 widgets dynamically adapt to any home-screen grid size:
1. **Next Prayer 2×2 Widget**: Oversized next prayer name, formatted time underneath, and a compact 5-prayer daily schedule table at the bottom.
2. **Daily Quran Verse 1×4 Widget**: Displays an inspiring Quranic verse with a dedicated **Next Verse** button to cycle verses in-place and a **Listen (`استماع`)** button that opens the Quran audio player directly to that Surah.
3. **Hijri Date 1×2 Widget**: Expressive Hijri & Gregorian date card that opens the Hijri Calendar & Events view on tap.
4. **Quick Qibla Circle 1×1 Widget**: Circular Qibla bearing & Kaaba distance badge that launches the Qibla compass on tap.
5. **Daily Prayer Timetable 1×4 Widget**: Full horizontal 5-prayer timetable highlighting the next prayer.

---

## 🎨 Design System & Typography

- **Material 3 Expressive**:
  - Supports **Material You Dynamic Colors** on Android 12+ (`API 31+`) with curated fallback palettes (**Twilight Plum**, **Oasis Emerald**, **Makkah Gold**) and true **AMOLED Dark Mode**.
  - Expressive pill-slider bottom navigation bar (`ExpressiveSliderBottomBar`) with smooth spring animations and zero divider clutter.
  - Full **English** and **Arabic (`العربية`)** localization with automatic **RTL** layout mirroring.
- **Bundled Typography**:
  - **Headlines & Titles**: `Roboto Serif` & `Roboto Flex`
  - **Timers, Coordinates & Badges**: `Roboto Mono`
  - **Arabic UI & Azkar**: `Noto Sans Arabic`
  - **Quranic Script**: `Amiri`

---

## 🏗️ Tech Stack & Project Structure

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose (Material 3, Adaptive Window Size Classes, Type-Safe Navigation Compose)
- **Home Screen Widgets**: Jetpack Glance (`glance-appwidget`, `glance-material3`)
- **Local Persistence**:
  - **Room Database (KSP)**: Cached Quran verses/Surahs, bookmarks, prayer completion logs, and custom Dhikr items.
  - **Preferences DataStore**: Calculation method, location coordinates, theme preferences, notification settings, and reciter selection.
- **Background & Media**: Foreground `Service` (`mediaPlayback`), `MediaPlayer` with `PARTIAL_WAKE_LOCK`, `AlarmManager`, and `BroadcastReceiver` (`BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`).
- **Testing**: JUnit 4 + Robolectric local JVM & Compose UI tests.

```text
app/src/main/java/com/example/
├── core/               # DI container, expressive shapes, i18n (EN/AR), formatting utilities
├── data/               # Room database, Preferences DataStore, Quran/Reciters/Azkar/Weather repositories
├── domain/             # Offline astronomical prayer engine, Hijri calendar converter, Qibla math
├── navigation/         # Type-safe Navigation Compose & ExpressiveSliderBottomBar
├── notifications/      # PrayerAlarmScheduler, PrayerAlarmReceiver & lock-screen NotificationHelper
├── presentation/       # MVVM screens & ViewModels (Home, Qibla, Quran Read & Listen, Dhikr, Calendar, Settings)
├── sensors/            # QiblaSensorManager (Rotation Vector + Magnetometer/Accelerometer fallback)
├── services/           # QuranAudioManager, QuranPlaybackService & AdhanAudioManager
├── ui/theme/           # Material 3 Expressive theme, dynamic color schemes & custom font families
└── widgets/            # 5 Jetpack Glance Home Screen widgets & receivers
```

---
## Screenshots
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/c58f9b34-92fd-491c-9fcf-8445428218c7" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/c9afbd96-cb5b-40f4-8677-ff55258ea76c" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/3e1ce4c3-29c1-4dbc-8962-7dc2f44fad51" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/3050aac0-fe98-440b-b25f-4213467665ce" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/754c7b00-5057-4a2a-afd9-675bda18dab7" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/e7b23c54-aaa6-4875-8887-d166ee9d9039" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/8996d554-280a-4cd2-a379-04d5617039ad" />
<img width="1069" height="1456" alt="image" src="https://github.com/user-attachments/assets/302e55b4-1fe4-4299-a2d8-c46214286693" />


## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17+**
- **Android SDK 35** (`minSdk = 24`, `targetSdk = 35`)

### Build & Run
1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd mihrab
   ```
2. Build the debug APK:
   ```bash
   gradle :app:assembleDebug
   ```
3. Run the unit and Robolectric UI test suite:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🔒 Privacy & Permissions

Mihrab is designed with a **local-first, privacy-respecting architecture**:
- **Location (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`)**: Used strictly on-device to compute astronomical prayer times and Qibla direction (or you can pick any city manually without granting location permission).
- **Notifications & Alarms (`POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`)**: Used for on-time prayer notifications and lock-screen countdown updates.
- **Foreground Media Playback & Wake Lock (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `WAKE_LOCK`)**: Used to keep Quran audio playing smoothly when the screen is turned off.
- **Internet (`INTERNET`)**: Used only for streaming/downloading Quran audio recitations, fetching optional Tanzil verses on demand, and updating local weather.
