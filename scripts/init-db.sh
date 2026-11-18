#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
H2_JAR=lib/h2-2.5.252.jar
[[ -f "$H2_JAR" ]] || { echo "Missing dependency: $H2_JAR" >&2; exit 1; }
database_path="${KOTLINBAZA_DB_PATH:-./data/access}"
java -cp "$H2_JAR" org.h2.tools.RunScript \
    -url "jdbc:h2:$database_path" -user sa -password '' -script scripts/init.sql
java -cp "$H2_JAR" org.h2.tools.RunScript \
    -url "jdbc:h2:$database_path" -user sa -password '' -script scripts/fill.sql
