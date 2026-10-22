
#!/bin/bash
set -e
shopt -s globstar nullglob

sources=(src/**/*.kt)

if [ ${#sources[@]} -eq 0 ]; then
    echo "Kotlin source files not found"
    exit 1
fi

CLI_JAR="lib/kotlinx-cli-jvm-0.3.6.jar"

if [ ! -f "$CLI_JAR" ]; then
    echo "kotlinx-cli library not found: $CLI_JAR"
    exit 1
fi

kotlinc "${sources[@]}" \
    -cp "$CLI_JAR" \
    -include-runtime \
    -d app.jar

# Добавляем библиотеку в Class-Path манифеста,
# чтобы java -jar могла найти kotlinx-cli.
MANIFEST_FILE="$(mktemp)"
trap 'rm -f "$MANIFEST_FILE"' EXIT

printf 'Class-Path: lib/kotlinx-cli-jvm-0.3.6.jar\n\n' > "$MANIFEST_FILE"
jar ufm app.jar "$MANIFEST_FILE"
