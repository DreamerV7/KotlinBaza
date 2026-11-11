#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
bundle="$(mktemp -d ./out/bundle.XXXXXX)"
mkdir -p "$bundle/out" "$bundle/lib" "$bundle/scripts" out/release
cp out/app.jar "$bundle/out/"
cp lib/kotlinx-cli-jvm-0.3.6.jar lib/h2-2.5.252.jar "$bundle/lib/"
cp scripts/run.sh scripts/init-db.sh scripts/init.sql scripts/fill.sql "$bundle/scripts/"
tar -czf out/release/kotlinbaza.tar.gz -C "$bundle" .
