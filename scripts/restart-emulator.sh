#!/usr/bin/env bash
# Kill stale/hidden emulator and cold-boot Pixel_6 with a visible window.
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"

AVD_NAME="${AVD_NAME:-Pixel_6}"
SNAPSHOT_DIR="$HOME/.android/avd/${AVD_NAME}.avd/snapshots/default_boot"
LOG="/tmp/kchat-emulator.log"

if ! command -v emulator >/dev/null 2>&1; then
  echo "Không tìm thấy emulator tại $ANDROID_HOME/emulator"
  echo "Cài SDK hoặc mở Android Studio → SDK Manager."
  exit 1
fi

echo "1/4 Dừng emulator cũ (nếu có)..."
adb devices 2>/dev/null | grep -E '^emulator-' | cut -f1 | while read -r serial; do
  adb -s "$serial" emu kill 2>/dev/null || true
done
sleep 2
pkill -f "qemu-system-aarch64.*-avd ${AVD_NAME}" 2>/dev/null || true
sleep 1

if ps aux | grep -q "[q]emu-system-aarch64.*-avd ${AVD_NAME}"; then
  echo "Không tắt được emulator. Thử đóng Android Studio → Device Manager → Stop."
  exit 1
fi

echo "2/4 Xóa snapshot lỗi (cold boot)..."
if [[ -d "$SNAPSHOT_DIR" ]]; then
  rm -rf "$SNAPSHOT_DIR"
  echo "   Đã xóa snapshot default_boot"
fi

echo "3/4 Khởi động emulator (cửa sổ riêng)..."
: >"$LOG"
nohup emulator -avd "$AVD_NAME" -gpu host -no-snapshot-load -no-snapshot-save >>"$LOG" 2>&1 &
EMU_PID=$!
echo "   PID: $EMU_PID — log: $LOG"

echo "4/4 Đợi boot..."
for _ in $(seq 1 90); do
  if adb devices 2>/dev/null | grep -qE '^emulator-[0-9]+\s+device$'; then
    booted="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
    if [[ "$booted" == "1" ]]; then
      echo "Emulator sẵn sàng (cửa sổ Pixel 6 sẽ hiện trên màn hình)."
      adb devices
      exit 0
    fi
  fi
  if ! kill -0 "$EMU_PID" 2>/dev/null; then
    echo "Emulator thoát sớm. Xem log:"
    tail -40 "$LOG"
    echo ""
    echo "Thử lại với GPU software:"
    echo "  emulator -avd $AVD_NAME -gpu swiftshader_indirect -no-snapshot-load"
    exit 1
  fi
  sleep 2
done

echo "Timeout chờ boot. Xem log: tail -f $LOG"
exit 1
