#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
shopt -s globstar nullglob
JUNIT_JAR=lib/junit-platform-console-standalone-1.12.2.jar
AGENT_JAR=lib/org.jacoco.agent-0.8.16-runtime.jar
REPORT_JAR=lib/org.jacoco.cli-0.8.16-nodeps.jar
for dependency in "$JUNIT_JAR" "$AGENT_JAR" "$REPORT_JAR"; do
    [[ -f "$dependency" ]] || { echo "Missing dependency: $dependency" >&2; exit 1; }
done
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) separator=';' ;;
    *) separator=':' ;;
esac
bash scripts/build.sh
coverage_dir="$(mktemp -d ./out/coverage.XXXXXX)"
test_sources=(tests/**/*.kt)
classpath="out/app.jar${separator}lib/kotlinx-cli-jvm-0.3.6.jar${separator}lib/h2-2.5.252.jar"
kotlinc "${test_sources[@]}" -cp "$JUNIT_JAR$separator$classpath" -d "$coverage_dir/tests.jar"
export KOTLINBAZA_COVERAGE_FILE="$coverage_dir/jacoco.exec"
java "-javaagent:$AGENT_JAR=destfile=$KOTLINBAZA_COVERAGE_FILE,append=false,excludes=kotlin.*:org.*:com.*" \
    -jar "$JUNIT_JAR" execute --class-path "$coverage_dir/tests.jar$separator$classpath" \
    --scan-class-path --fail-if-no-tests --reports-dir out/test-results
bash scripts/smoke-test.sh

# Only application classes belong in the report, not the bundled Kotlin runtime.
# The current source files all use the default package.
if command -v jar >/dev/null 2>&1; then
    jar_tool="$(command -v jar)"
else
    jdk_home="${JAVA_HOME:-$(java -XshowSettings:properties -version 2>&1 | sed -n 's/^[[:space:]]*java.home = //p' | tr -d '\r')}"
    jar_tool="${jdk_home//\\//}/bin/jar"
fi
classes=()
while IFS= read -r entry; do
    entry="${entry%$'\r'}"
    case "$entry" in
        */*) ;;
        *.class) classes+=("$entry") ;;
    esac
done < <("$jar_tool" tf out/app.jar)
[[ ${#classes[@]} -gt 0 ]] || { echo 'No application classes found' >&2; exit 1; }
mkdir -p "$coverage_dir/classes" out/coverage
(
    cd "$coverage_dir/classes"
    "$jar_tool" xf ../../app.jar "${classes[@]}"
)
source_options=()
for source_dir in src src/*/; do
    source_options+=(--sourcefiles "$source_dir")
done
java -jar "$REPORT_JAR" report "$KOTLINBAZA_COVERAGE_FILE" \
    --classfiles "$coverage_dir/classes" "${source_options[@]}" \
    --xml out/coverage/jacoco.xml --html out/coverage/html --encoding UTF-8
