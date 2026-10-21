#!/bin/bash
set -e
shopt -s globstar nullglob
sources=(src/**/*.kt)
if [ ${#sources[@]} -eq 0 ]; then
  echo "Kotlin source files not found"
  exit 1
fi
kotlinc "${sources[@]}" -cp "lib/kotlinx-cli-jvm-0.3.6.jar" -include-runtime -d app.jar
