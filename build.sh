#!/bin/bash

kotlinc src/Main.kt \
  -cp "lib/kotlinx-cli-jvm-0.3.6.jar" \
  -include-runtime \
  -d app.jar