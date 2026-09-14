#!/usr/bin/env bash
# HealthFirst PIMS launcher (requires Java 21 or later).
set -euo pipefail
cd "$(dirname "$0")"
java -jar healthfirst-pims-1.0.0.jar
