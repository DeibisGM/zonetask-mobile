# ZoneTask Mobile

ZoneTask Mobile is the Android client for ZoneTask. It helps people organize shared spaces through floor plans, zones, tasks, assignments, chat, notifications, and reports.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Navigation Compose
- Retrofit and Gson for REST communication
- Coil for images and SVG assets
- Firebase Cloud Messaging
- Coroutines, Flow, and Android ViewModels

## Project Layout

```text
app/src/main/java/com/app/zonetask/
  core/          Session, constants, local state and shared helpers
  data/          Retrofit services, DTOs and repositories
  di/            Application dependency container
  domain/        Domain models and conversions
  messaging/     Firebase messaging and notification support
  navigation/    Navigation graphs, destinations and navigation actions
  ui/components/ Reusable Compose components
  ui/screens/    Feature screens and ViewModels
  ui/theme/      Colors, typography and Compose theme

app/src/main/assets/zone-objects/
  SVG assets used by the floor-plan object editor
```

## Features

| Area | Included flows |
| --- | --- |
| Authentication | Login, registration, password reset and user session handling. |
| Spaces | Select, create, manage and invite members to shared spaces. |
| Tasks | Create tasks, assign members, track completion, view history and task rotation. |
| Floor plans | Create plans, draw zones, resize and move zones, apply templates and save layouts. |
| Zone objects | Place, move, delete and rotate furniture inside a selected zone. |
| Home | View the active floor, selected zone tasks and pending work. |
| Chat | Space chat with real-time updates and image messages. |
| Reports | Space and member statistics, reports and overdue trends. |

## Requirements

- Android Studio
- JDK 11 or newer
- Android SDK with `compileSdk 36`
- Android emulator or an Android device with USB debugging enabled
- A running ZoneTask API instance reachable from the device

## Configure the API URL

Set `apiBaseUrl` in `local.properties`:

```properties
apiBaseUrl=http://192.168.0.213:5248/
```

Use an address the device can reach:

- Physical phone: use the computer's LAN IP address.
- Standard Android emulator: use `http://10.0.2.2:5248/`.
- Never use `localhost` for a physical phone, because it points to the phone itself.

`local.properties` is machine-specific and must not be committed.

## Build

From this directory:

```powershell
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To install a debug build on a connected device:

```powershell
./gradlew :app:installDebug
```

From the workspace root, `run-app.bat` checks for an emulator, builds the APK, installs it through ADB, and launches the app.

## Floor Plan Editor

The editor uses a two-level grid:

- One large square represents `1 m x 1 m`.
- Four fine cells represent one metre in each axis.
- A fine cell is `25 cm x 25 cm`.

Zones can be selected, moved, resized with corner handles, or resized by entering width and height values in metres. A zone must be selected before it can move.

## Objects Inside Zones

Open a selected zone and choose **Edit objects** to enter its focused object editor. The initial catalog contains:

| Object | Footprint |
| --- | --- |
| Sofa | `2 m x 1 m` |
| Bed | `1 m x 2 m` |
| Table and chairs | `2 m x 2 m` |

The editor displays only the one-metre squares for the selected zone. Objects use their original SVG assets from `app/src/main/assets/zone-objects/` and can be moved or rotated in 90-degree increments.

The app blocks these invalid operations:

- Adding an object that does not fit in the zone.
- Moving an object outside the zone.
- Rotating an object when the rotated footprint does not fit.
- Shrinking a zone in a way that would cut off an object.

Object positions are local to a zone. When a zone moves, its objects move visually with it. Saving the floor sends zones and their object layouts to the API.

## Notifications

The application asks for notification permission on Android 13 and newer. Firebase configuration is required for push notifications. Verify `google-services.json` and backend Firebase credentials before testing notification delivery.

## Troubleshooting

| Problem | Check |
| --- | --- |
| Build fails | Confirm Android SDK 36, JDK, Gradle wrapper access, and dependencies. |
| API calls fail | Check `apiBaseUrl`, API port `5248`, device network access, and backend CORS. |
| App installs but cannot open | Use `adb devices` to confirm the emulator or phone is connected. |
| SVG objects are missing | Confirm the SVG files exist under `app/src/main/assets/zone-objects/` and Coil SVG is included. |
| Object layout does not persist | Verify the backend is running the latest build and the database `zone_object` schema is updated. |
