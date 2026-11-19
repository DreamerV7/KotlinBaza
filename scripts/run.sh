#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
java_options=()
if [[ -n "${KOTLINBAZA_COVERAGE_FILE:-}" ]]; then
    java_options+=("-javaagent:target/tools/org.jacoco.agent-0.8.16-runtime.jar=destfile=$KOTLINBAZA_COVERAGE_FILE,append=true")
fi
java "${java_options[@]}" -jar out/app.jar "$@"
