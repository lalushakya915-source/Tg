#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
TD_DIR="$PROJECT_DIR/td"

echo "== TGDownloader: official TDLib setup =="

if ! command -v git >/dev/null 2>&1; then
  echo "git is required"; exit 1
fi

if [ ! -d "$TD_DIR/.git" ]; then
  git clone https://github.com/tdlib/td.git "$TD_DIR"
else
  echo "TDLib repository already exists; leaving its revision unchanged."
fi

cd "$TD_DIR/example/android"

chmod +x check-environment.sh fetch-sdk.sh build-openssl.sh build-tdlib.sh

./check-environment.sh
./fetch-sdk.sh
./build-openssl.sh
./build-tdlib.sh

cd "$PROJECT_DIR"

rm -rf app/src/main/java/org/drinkless/tdlib
mkdir -p app/src/main/java/org/drinkless/tdlib
cp -R td/example/android/tdlib/java/org/drinkless/tdlib/. \
      app/src/main/java/org/drinkless/tdlib/

rm -rf app/src/main/jniLibs
mkdir -p app/src/main/jniLibs
cp -R td/example/android/tdlib/libs/. \
      app/src/main/jniLibs/

echo
echo "TDLib integration complete."
echo "Now set TELEGRAM_API_ID and TELEGRAM_API_HASH in gradle.properties."
