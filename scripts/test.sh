#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
shopt -s globstar nullglob
JUNIT_JAR=lib/junit-platform-console-standalone-1.12.2.jar
CLI_JAR=lib/kotlinx-cli-jvm-0.3.6.jar
H2_JAR=lib/h2-2.5.252.jar
for dependency in "$JUNIT_JAR" "$CLI_JAR" "$H2_JAR"; do
    [[ -f "$dependency" ]] || { echo "Missing dependency: $dependency" >&2; exit 1; }
done
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) separator=';' ;;
    *) separator=':' ;;
esac
sources=(src/**/*.kt tests/**/*.kt)
mkdir -p out
kotlinc "${sources[@]}" -cp "$JUNIT_JAR$separator$CLI_JAR$separator$H2_JAR" -include-runtime -d out/tests.jar
java -jar "$JUNIT_JAR" execute \
    --class-path "out/tests.jar$separator$CLI_JAR$separator$H2_JAR" \
    --scan-class-path --fail-if-no-tests --reports-dir out/test-results
