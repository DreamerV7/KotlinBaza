#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
bundle="$(mktemp -d ./out/bundle.XXXXXX)"
mkdir -p "$bundle/out" "$bundle/target/dependency" "$bundle/scripts" out/release
cp out/app.jar "$bundle/out/"
cp target/dependency/h2-2.5.252.jar "$bundle/target/dependency/"
cp scripts/run.sh scripts/init-db.sh scripts/init.sql scripts/fill.sql "$bundle/scripts/"
tar -czf out/release/kotlinbaza.tar.gz -C "$bundle" .
