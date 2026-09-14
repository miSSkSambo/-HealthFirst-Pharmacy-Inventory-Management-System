#!/usr/bin/env bash
# Captures required evidence screens in a 1280x800 virtual desktop.
# Requires the database script to have been imported and xvfb, scrot, and xdotool.
set -euo pipefail
cd "$(dirname "$0")/.."
setxkbmap -layout us
rm -f screenshots/*.png
java -jar target/healthfirst-pims-1.0.0.jar &
APP_PID=$!
cleanup() { kill "$APP_PID" 2>/dev/null || true; }
trap cleanup EXIT
sleep 3
scrot screenshots/01-login-screen.png

# Sign in as administrator.
xdotool mousemove 650 385 click 1
xdotool type --delay 40 admin
xdotool mousemove 650 427 click 1
xdotool type --delay 40 admin123
xdotool key Return
sleep 3
scrot screenshots/02-admin-dashboard.png

# Medicine CRUD table with seeded records.
xdotool mousemove 245 98 click 1
sleep 2
scrot screenshots/03-manage-medicines.png

# Sales report followed by the other required reports.
xdotool mousemove 620 98 click 1
sleep 2
scrot screenshots/04-sales-report.png
xdotool mousemove 635 140 click 1
sleep 2
scrot screenshots/05-item-wise-sales-report.png
xdotool mousemove 768 140 click 1
sleep 2
scrot screenshots/06-low-stock-report.png
xdotool mousemove 1075 140 click 1
sleep 2
scrot screenshots/07-expiry-report.png

# Log out, sign in as cashier, and produce a bill with a cart item.
xdotool mousemove 1190 52 click 1
sleep 2
xdotool mousemove 650 385 click 1
xdotool type --delay 40 cashier
xdotool mousemove 650 427 click 1
xdotool type --delay 40 cash123
xdotool key Return
sleep 3
xdotool mousemove 260 184 click 1
xdotool mousemove 608 738 click 1
sleep 2
scrot screenshots/08-cashier-pos-with-cart.png
xdotool mousemove 1130 172 click 1
sleep 3
scrot screenshots/09-generated-bill.png
