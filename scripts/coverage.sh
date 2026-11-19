#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
bash mvnw -B clean verify
mkdir -p out
cp target/kotlinbaza.jar out/app.jar
bash mvnw -B dependency:copy -Dartifact=org.jacoco:org.jacoco.agent:0.8.16:jar:runtime -DoutputDirectory=target/tools
export KOTLINBAZA_COVERAGE_FILE=target/jacoco.exec
bash scripts/smoke-test.sh
bash mvnw -B jacoco:report
