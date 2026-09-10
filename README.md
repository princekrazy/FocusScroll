# FocusScroll

> Use Instagram and YouTube without getting trapped in endless short-form content.

FocusScroll is an Android app designed to help reduce mindless scrolling by allowing access to useful parts of **Instagram and YouTube** while automatically blocking their short-form video experiences.

Instead of replacing these apps or requiring a modified version of them, FocusScroll works alongside them using Android's **AccessibilityService API** to detect when a user enters content they have chosen to avoid.

##  Features

### Instagram

*  Direct Messages
*  Profiles
*  Regular posts
*  Instagram Reels

### YouTube

*  Search
*  Subscriptions
*  Individual videos
*  YouTube Shorts

The goal is not to completely block social media. The goal is to remove the **infinite short-form feed** while keeping useful functionality available.

---

##  How It Works

FocusScroll uses an Android `AccessibilityService` to monitor the user interface of supported applications.

The service can inspect accessibility information exposed by apps, such as:

* Text
* Content descriptions
* View types
* UI hierarchy
* Package names

FocusScroll uses these signals to determine whether the user is currently viewing a Reel or Short.

### 1. Detect the App

Whenever the accessibility service receives a UI event, it checks which application generated the event.

FocusScroll currently monitors:

```text
com.instagram.android
com.google.android.youtube
```

Other applications are ignored.

### 2. Inspect the UI

FocusScroll retrieves the current accessibility node tree using:

```kotlin
rootInActiveWindow
```

The service recursively searches through the UI hierarchy for identifiers associated with short-form content.

### 3. Detect Instagram Reels

Instagram exposes a useful accessibility description for Reel playback screens.

A Reel can contain a description similar to:

```text
Reel by username. Double tap to play or pause.
```

FocusScroll searches the accessibility tree for a description that:

1. Starts with `Reel by`
2. Contains `Double tap to play or pause`

For example:

```kotlin
if (
    description.startsWith("Reel by ", ignoreCase = true) &&
    description.contains(
        "Double tap to play or pause",
        ignoreCase = true
    )
) {
    return true
}
```

This allows FocusScroll to detect Reels without relying on a specific username or hardcoded Reel.

### 4. Detect YouTube Shorts

YouTube uses different accessibility information.

Instead of simply searching for the word `Shorts`, FocusScroll looks for a more specific UI element exposed on Shorts.

Shorts currently expose a button with a content description similar to:

```text
Remix
```

FocusScroll searches the accessibility tree for an Android button whose content description is `Remix`.

```kotlin
if (
    node.className?.toString() == "android.widget.Button" &&
    description.equals("Remix", ignoreCase = true)
) {
    return true
}
```

This is more reliable than checking whether the screen contains the word `Shorts`, because the word `Shorts` can also appear in normal YouTube navigation.

### 5. Block the Content

When a Reel or Short is detected, FocusScroll uses:

```kotlin
performGlobalAction(GLOBAL_ACTION_BACK)
```

This sends the user back to the previous screen.

A short cooldown is also used so that repeated accessibility events do not trigger the back action continuously.

---

## ️ Architecture

```text
┌─────────────────────┐
│     FocusScroll     │
│    Android App      │
└──────────┬──────────┘
           │
           │ Enables
           ▼
┌─────────────────────┐
│ AccessibilityService│
└──────────┬──────────┘
           │
           │ Monitors UI events
           ▼
┌─────────────────────────────┐
│      Active Application     │
│                             │
│  Instagram / YouTube        │
└─────────────┬───────────────┘
              │
              │ Accessibility Tree
              ▼
┌─────────────────────────────┐
│      FocusScroll Detector   │
│                             │
│  Instagram → Reel pattern   │
│  YouTube → Remix button     │
└─────────────┬───────────────┘
              │
          Detected?
          /      \
        No        Yes
        │          │
        ▼          ▼
      Allow      Go Back
                   │
                   ▼
             Previous Screen
```

---

## ️ Tech Stack

* **Kotlin**
* **Android**
* **Jetpack Compose**
* **Android AccessibilityService**
* **Material 3**
* **Gradle**

No backend, database, or account system is required.

---

##  Installation

### From an APK

1. Download the latest APK from the [Releases](../../releases) page.
2. Install the APK on an Android device.
3. Open FocusScroll.
4. Tap **Enable FocusScroll**.
5. Enable FocusScroll in Android Accessibility settings.
6. Open Instagram or YouTube and test the blocking behavior.

### From Source

Clone the repository:

```bash
git clone https://github.com/princekrazy/FocusScroll.git
```

Open the project in **Android Studio** and build/run it on an Android device or emulator.

---

##  Accessibility Permission

FocusScroll requires Android's Accessibility Service permission because it needs to inspect the UI of supported applications and detect when short-form content is being displayed.

The service is used specifically for:

* Detecting Instagram Reels
* Detecting YouTube Shorts
* Returning the user to the previous screen when detected

FocusScroll does not require a user account or backend server.

---

##  Why I Built This

Short-form video is useful, but the infinite feed design can make it difficult to stop watching.

I wanted a middle ground:

> **Keep the useful parts of social media. Remove the parts designed to keep me scrolling.**

FocusScroll explores whether Android's accessibility APIs can be used to create a more intentional way of interacting with existing social media platforms.

---

## ️ Current Limitations

FocusScroll is currently an MVP.

Accessibility identifiers can change when Instagram or YouTube update their applications. Because the detection logic relies on information exposed through their accessibility trees, future app updates may require changes to the detection algorithms.

Current scope:

* Instagram Reels are blocked.
* YouTube Shorts are blocked.
* Instagram Explore/infinite feed is not currently blocked.
* YouTube recommendation feeds are not currently blocked.
* Detection is based on the current accessibility information exposed by the applications.

---

##  Future Ideas

Potential improvements include:

* Block Instagram Explore
* Reduce Instagram infinite scrolling
* Block YouTube Shorts shelves
* Limit recommendation feeds
* Add customizable blocking rules
* Add usage statistics
* Add configurable cooldowns
* Improve detection resilience across app updates
* Add an allow/disable toggle
* Add a cleaner settings screen

---

##  Project Structure

```text
app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/focusscroll/
│       │       ├── MainActivity.kt
│       │       └── FocusScrollAccessibilityService.kt
│       │
│       ├── res/
│       │   ├── drawable/
│       │   ├── mipmap/
│       │   ├── values/
│       │   └── xml/
│       │       └── accessibility_service_config.xml
│       │
│       └── AndroidManifest.xml
│
└── build.gradle.kts
```

---

## Author

**Prince Kuvenga**

Computer Science student and software developer building projects focused on practical problems.

* GitHub: [@princekrazy](https://github.com/princekrazy)
* Portfolio: [princekuvenga.com](https://princekuvenga.com)

---

##  License

This project is currently for educational and personal use.
