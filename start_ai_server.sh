#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
MAC_IP=$(ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null || echo "127.0.0.1")

echo "=========================================================="
echo "🚀 AgeTrace Deep Learning AI Server (Port 8080)"
echo "📡 Mac IP: http://$MAC_IP:8080"
echo "=========================================================="

# Auto reverse for any connected Android devices (USB or Wireless ADB)
if command -v adb >/dev/null 2>&1; then
    adb reverse tcp:8080 tcp:8080 2>/dev/null && echo "✅ ADB reverse aktifleştirildi: Telefon 127.0.0.1:8080 üzerinden doğrudan Mac'e bağlandı."
fi

"$DIR/python/.venv/bin/python" "$DIR/python/age_trace_ai.py" --serve --port 8080

