# k-chat Android

> Client chat nội bộ · Jetpack Compose · REST + WebSocket · FCM

| | | |
|:--|:--|:--|
| **Package** | `com.kchat` | **Version** `0.2.0-phase4` |
| **SDK** | min 26 · target 35 | **JDK** 17 |

Mở **`k-chat/android`** trong Android Studio (không phải root `kpay/`).

---

## Tech stack

| Layer | Công nghệ |
|:------|:----------|
| UI | Jetpack Compose · Material 3 · Coil |
| Architecture | MVVM · Hilt · Navigation Compose |
| Network | Retrofit · OkHttp · Kotlin Serialization |
| Realtime | OkHttp WebSocket |
| Local | Room — cache messages & rooms |
| Background | WorkManager — TTL cache cleanup |
| Push | Firebase Cloud Messaging (data-only) |
| Calls | WebRTC — signaling qua WS |
| Build | Gradle Kotlin DSL · KSP |

---

## Vị trí trong hệ thống

```mermaid
flowchart LR
    subgraph device["Thiết bị Android"]
        APP["k-chat App"]
        ROOM[("Room DB<br/>offline cache")]
        FCM_SVC["FCM Service"]
        APP --- ROOM
        APP --- FCM_SVC
    end

    subgraph cloud["k-chat-api"]
        REST["REST :8864"]
        WS["WebSocket /ws"]
        PUSH["FCM Admin"]
    end

    APP -->|"HTTPS"| REST
    APP <-->|"WSS + JWT"| WS
    PUSH -->|"data push"| FCM_SVC
    FCM_SVC --> APP
```

> Kiến trúc tổng thể: [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md) · Backend: [`../backend/README.md`](../backend/README.md)

---

## Kiến trúc module

```mermaid
flowchart TB
    APP["<b>:app</b><br/>MainActivity · Application<br/>FCM · WorkManager"]

    subgraph core["Core"]
        direction LR
        DESIGN["design<br/>Theme"]
        MODEL["model<br/>Domain"]
        UI["ui<br/>Screens"]
        NAV["navigation<br/>ViewModels · NavHost"]
    end

    subgraph data["Data"]
        direction LR
        REPO["repository<br/>Contracts · SessionCoordinator"]
        NET["network<br/>Retrofit · WS"]
        LOCAL["local<br/>Room"]
        FAKE["fake<br/>Preview data"]
    end

    API["k-chat-api"]

    APP --> NAV
    NAV --> UI & REPO
    UI --> DESIGN & MODEL
    REPO --> NET & LOCAL
    REPO -.-> FAKE
    NET --> API
```

| Module | Trách nhiệm |
|:-------|:------------|
| `:app` | Entry point · DI root · push · background jobs |
| `:core:design` | Theme, typography, dimens |
| `:core:model` | Models, validation rules |
| `:core:ui` | Compose screens & shared components |
| `:core:navigation` | Routes, NavHost, ViewModels |
| `:data:repository` | Repository interfaces · session · realtime |
| `:data:network` | REST client · WebSocket · mappers |
| `:data:local` | Room entities · DAO · cache purge |
| `:data:fake` | Sample / preview data (Compose previews) |

---

## Navigation app

```mermaid
flowchart TB
    AUTH["Auth<br/>Login · Register · OTP · Reset PW"]
    PIN["PIN lock"]
    HOME["Home · Tab Chat · Contacts · Settings"]
    ROOM["Chat room"]
    SET["Settings<br/>Profile · Storage · Devices · Theme"]

    AUTH --> PIN --> HOME
    HOME --> ROOM
    HOME --> SET
    ROOM -->|"menu"| SET
```

---

## Luồng chính

### Session sau login

```mermaid
sequenceDiagram
    autonumber
    box rgba(200,220,255,0.3) Android
        participant UI as LoginScreen
        participant SC as SessionCoordinator
        participant WS as RealtimeCoordinator
    end
    box rgba(200,255,200,0.3) Backend
        participant API as k-chat-api
    end

    UI->>API: POST /auth/login
    API-->>UI: JWT access + refresh
    UI->>SC: onAuthenticated()
    SC->>API: register device · sync FCM token
    SC->>WS: connect WebSocket
    SC->>API: refresh rooms · contacts · settings
    SC->>SC: schedule cache cleanup
    WS-->>UI: realtime sẵn sàng
```

### Nhận tin (online + offline)

```mermaid
sequenceDiagram
    autonumber
    participant S as Sender app
    participant API as k-chat-api
    participant R as Receiver app
    participant DB as Room cache
    participant FCM as Firebase

    S->>API: POST /rooms/{id}/messages
    API-->>R: WS message_new
    R->>DB: appendMessageAndTouchRoom
    DB-->>R: UI cập nhật (Flow)

    alt App background / WS ngắt
        API->>FCM: data push
        FCM-->>R: onMessageReceived
        R->>R: show notification
    end
```

### Dọn cache local (#20)

```mermaid
flowchart TB
    T["Trigger<br/>login · foreground · đổi settings · worker hàng ngày"]
    W["LocalCacheCleanupWorker"]
    P["purgeExpiredLocalMessages()"]

    T --> W --> P

    P --> R1["Room có disappearing TTL<br/>→ xóa tin quá hạn theo room"]
    P --> R2["Room không TTL<br/>→ xóa tin cũ hơn 7 / 30 / 90 ngày"]
```

---

## Cấu hình

### API URL

File: [`app/build.gradle.kts`](app/build.gradle.kts) → `buildTypes.debug` / `release`

| Môi trường | Build | REST | WebSocket | Banner login |
|:-----------|:------|:-----|:----------|:-------------|
| **Staging** *(debug mặc định)* | `./gradlew :app:assembleDebug` | `https://chat-api-test.tayjava.net/` | `wss://chat-api-test.tayjava.net` | `Staging` |
| **Local emulator** | `./gradlew :app:assembleDebug -Pkchat.env=local` | `http://10.0.2.2:8864/` | `ws://10.0.2.2:8864` | `Local` |
| **Prod** | `./gradlew :app:assembleRelease` | `https://chat-api.safep4y.com/` | `wss://chat-api.safep4y.com` | *(ẩn)* |

\* Local cần BE `:8864` + seed — xem [`../backend/README.md`](../backend/README.md)

Cleartext HTTP (`10.0.2.2`): [`network_security_config.xml`](app/src/main/res/xml/network_security_config.xml)  
Thiết bị thật + BE local: sửa `kchatLocalApiUrl` trong `build.gradle.kts` thành IP LAN máy host.

### BuildConfig

| Field | Mô tả |
|:------|:------|
| `API_BASE_URL` | Base URL REST |
| `WS_BASE_URL` | WebSocket host |
| `APP_ENV` | `local` / `staging` / `prod` — điều khiển banner |
| `FCM_ENABLED` | Tự `true` khi có `google-services.json` |

### FCM

```mermaid
flowchart LR
    JSON["google-services.json<br/>app/"] --> GRADLE["build.gradle.kts"]
    GRADLE --> FLAG["FCM_ENABLED = true"]
    FLAG --> SVC["KChatFirebaseMessagingService"]
    BE["BE staging FCM<br/>enable-staging-kchat-fcm.sh"] --> PUSH["FCM data push"]
    PUSH --> SVC
```

| | |
|:--|:--|
| File | `app/google-services.json` (package `com.kchat`) |
| Hướng dẫn tạo Firebase | [`../docs/FIREBASE_SETUP.md`](../docs/FIREBASE_SETUP.md) |
| Thiếu file | App chạy bình thường, **không** có push OS |
| BE staging | `./infra/scripts/enable-staging-kchat-fcm.sh` (từ root `kpay/`) |

---

## Build & chạy

**Cần:** Android Studio · JDK 17 · SDK 35 · emulator hoặc device

```bash
cd k-chat/android

./gradlew :app:assembleDebug                         # staging
./gradlew :app:assembleDebug -Pkchat.env=local       # local BE :8864
./gradlew :app:installDebug                          # build + cài device
./gradlew :app:assembleRelease                       # prod APK — chat-api.safep4y.com
```

| Output | Path |
|:-------|:-----|
| APK debug | `app/build/outputs/apk/debug/app-debug.apk` |
| APK release (prod) | `app/build/outputs/apk/release/app-release.apk` |

```mermaid
flowchart LR
    A["Android Studio<br/>Run ▶ app"] 
    B["./scripts/run-app.sh"]
    C["./scripts/restart-emulator.sh"]

    A --> D["installDebug"]
    B --> D
    C --> B
    D --> E["MainActivity"]
```

| Cách | Ghi chú |
|:-----|:--------|
| **Studio** | Run config module `app` (`k-chat.app`) |
| **Script** | `chmod +x scripts/*.sh && ./scripts/run-app.sh` |
| **Emulator lỗi** | `./scripts/restart-emulator.sh` → cold boot |

```bash
# adb not found
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools
```

---

## Phân phối & tích hợp BE

App **không deploy server** — build APK nội bộ.

| Việc | Thực hiện |
|:-----|:----------|
| **Prod go-live** | [`PROD_DEPLOY.md`](../docs/PROD_DEPLOY.md) |
| APK prod (nhân viên) | `./gradlew :app:assembleRelease` → `chat-api.safep4y.com` |
| APK debug (staging) | `./gradlew :app:assembleDebug` |
| Deploy API prod | `./infra/scripts/deploy-prod-kchat.sh --wait` |
| Bật FCM prod | `./infra/scripts/enable-prod-kchat-fcm.sh` |

**Release** → prod · **Debug** → staging.

**Smoke test push:** 2 máy · 2 user · receiver ở Home · notification shade.

---

## Xử lý lỗi

| Triệu chứng | Nguyên nhân / xử lý |
|:------------|:--------------------|
| Login fail (staging) | Staging không seed user → **đăng ký mới** |
| Connection refused | BE chưa chạy hoặc sai `API_BASE_URL` |
| Cleartext failed | Chưa bật URL local / thiếu network security config |
| Không có push | Thiếu `google-services.json` · quyền notification · BE FCM off |
| `<no module>` Run | Edit Configurations → Module → `k-chat.app` |
| Gradle sync fail | Mở đúng `k-chat/android` · JDK 17 |

---

## Tài liệu

| | |
|:--|:--|
| Yêu cầu | [`REQUIREMENTS.md`](../docs/REQUIREMENTS.md) |
| QA test cases | [`ANDROID_QA_TESTCASES.md`](../docs/ANDROID_QA_TESTCASES.md) |
| UI plan | [`ANDROID_UI_PLAN.md`](../docs/ANDROID_UI_PLAN.md) |
| Wireframe | [`UI_MOCKUP.md`](../docs/UI_MOCKUP.md) |
| Backend | [`backend/README.md`](../backend/README.md) |
| Backlog | [`BACKLOG.md`](../docs/BACKLOG.md) |
| **Prod deploy** | [`PROD_DEPLOY.md`](../docs/PROD_DEPLOY.md) |
| **Firebase (FCM)** | [`FIREBASE_SETUP.md`](../docs/FIREBASE_SETUP.md) |
