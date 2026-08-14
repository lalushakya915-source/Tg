# Phone-only APK build

PC की जरूरत नहीं है।

## 1. GitHub repository बनाएं
GitHub app/browser से नया repository बनाएं और इस project की सभी files upload करें।

## 2. Telegram API credentials
GitHub repository में:

Settings → Secrets and variables → Actions → New repository secret

दो secrets बनाएं:

TELEGRAM_API_ID
TELEGRAM_API_HASH

ये credentials https://my.telegram.org से मिलते हैं।

## 3. Build
Repository में:

Actions → Build TGDownloader APK → Run workflow

Build पूरा होने पर:

Actions → latest workflow run → Artifacts → TGDownloader-debug

ZIP डाउनलोड करके उसमें APK मिलेगा।

## Important
GitHub Actions में build काफी समय ले सकता है क्योंकि TDLib source और native Android libraries compile होती हैं।
