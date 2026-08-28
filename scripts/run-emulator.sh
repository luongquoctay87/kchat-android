#!/usr/bin/env bash
# Start Android emulator (Pixel_6) if none is running.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"

AVD_NAME="${AVD_NAME:-Pixel_6}"

if ! command -v emulator >/dev/null 2>&1; then
  echo "Không tìm thấy emulator. Kiểm tra SDK tại: $ANDROID_HOME"
  echo "Hoặc mở Android Studio → Device Manager → chạy Pixel_6."
  exit 1
fi

# Emulator chạy ẩn (Android Studio / agent) — user không thấy cửa sổ
if ps aux | grep -q "[q]emu-system-aarch64.*-avd ${AVD_NAME}.*-qt-hide-window"; then
  echo "Emulator đang chạy ẨN (không có cửa sổ). Khởi động lại..."
  exec "$ROOT/scripts/restart-emulator.sh"
fi

if adb devices | grep -qE '^emulator-[0-9]+\s+device$'; then
  echo "Emulator đã chạy:"
  adb devices
  echo "Nếu không thấy cửa sổ: ./scripts/restart-emulator.sh"
  exit 0
fi

echo "Emulator chưa chạy — cold boot $AVD_NAME ..."
exec "$ROOT/scripts/restart-emulator.sh"
