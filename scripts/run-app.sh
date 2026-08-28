#!/usr/bin/env bash
# Build, install, and launch k-chat on emulator/device.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"

"$ROOT/scripts/run-emulator.sh"

cd "$ROOT"
echo "Build & install debug APK..."
./gradlew :app:installDebug

echo "Mở app k-chat..."
adb shell am start -n com.kchat/.MainActivity

echo "Xong. Network: nguyenva / password (BE :8864). Fake: -Pkchat.useFake=true"
