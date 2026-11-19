#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
bash mvnw -B package -DskipTests
mkdir -p out
cp target/kotlinbaza.jar out/app.jar
