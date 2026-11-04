
#!/bin/bash
set -e
cd "$(dirname "${BASH_SOURCE[0]}")/.."
shopt -s globstar nullglob

sources=(src/**/*.kt)

if [ ${#sources[@]} -eq 0 ]; then
    echo "Kotlin source files not found"
    exit 1
fi

CLI_JAR="lib/kotlinx-cli-jvm-0.3.6.jar"
H2_JAR="lib/h2-2.5.252.jar"
if [ ! -f "$H2_JAR" ]; then
    echo "H2 library not found: $H2_JAR" >&2
    exit 1
fi

if [ ! -f "$CLI_JAR" ]; then
    echo "kotlinx-cli library not found: $CLI_JAR"
    exit 1
fi

mkdir -p out
kotlinc "${sources[@]}" \
    -cp "$CLI_JAR" \
    -include-runtime \
    -d out/app.jar

# Добавляем библиотеку в Class-Path манифеста,
# чтобы java -jar могла найти kotlinx-cli.
MANIFEST_FILE="$(mktemp)"
trap 'rm -f "$MANIFEST_FILE"' EXIT

printf 'Class-Path: ../lib/kotlinx-cli-jvm-0.3.6.jar ../lib/h2-2.5.252.jar\n\n' > "$MANIFEST_FILE"
if command -v jar >/dev/null 2>&1; then
    jar ufm out/app.jar "$MANIFEST_FILE"
else
    jdk_home="${JAVA_HOME:-$(java -XshowSettings:properties -version 2>&1 | sed -n 's/^[[:space:]]*java.home = //p' | tr -d '\r')}"
    jdk_home="${jdk_home//\\//}"
    "$jdk_home/bin/jar" ufm out/app.jar "$MANIFEST_FILE"
fi
